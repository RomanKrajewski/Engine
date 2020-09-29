/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.FilterSettings;
import com.terraforged.core.tile.Size;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.world.GeneratorContext;

import java.util.Random;
import java.util.function.IntFunction;

public class Erosion implements Filter {

    private static final int erosionRadius = 3;
    private static final float inertia = 0.05f; // At zero, water will instantly change direction to flow downhill. At 1, water will never change direction.
    private static final float sedimentCapacityFactor = 4; // Multiplier for how much sediment a droplet can carry
    private static final float minSedimentCapacity = 0.01f; // Used to prevent carry capacity getting too close to zero on flatter terrain
    private static final float evaporateSpeed = 0.01f;
    private static final float gravity = 3;

    private final float erodeSpeed;
    private final float depositSpeed;
    private final float initialSpeed;
    private final float initialWaterVolume;
    private final int maxDropletLifetime;
    private final int[][] erosionBrushIndices;
    private final float[][] erosionBrushWeights;

    private final int mapSize;
    private final Modifier modifier;

    public Erosion(int mapSize, FilterSettings.Erosion settings, Modifier modifier) {
        this.mapSize = mapSize;
        this.modifier = modifier;
        this.erodeSpeed = settings.erosionRate;
        this.depositSpeed = settings.depositeRate;
        this.initialSpeed = settings.dropletVelocity;
        this.initialWaterVolume = settings.dropletVolume;
        this.maxDropletLifetime = settings.dropletLifetime;
        this.erosionBrushIndices = new int[mapSize * mapSize][];
        this.erosionBrushWeights = new float[mapSize * mapSize][];
        initBrushes(mapSize, erosionRadius);
    }

    public int getSize() {
        return mapSize;
    }

    @Override
    public void apply(Filterable map, int seedX, int seedZ, int iterations) {
        applyMain(map, seedX, seedZ, iterations, new Random());
    }

    private int nextCoord(Size size, Random random) {
        return random.nextInt(size.total - 1);
    }

    private void applyMain(Filterable map, int seedX, int seedZ, int iterations, Random random) {
        final int size = map.getSize().total;
        final Cell[] cells = map.getBacking();

        float posX;
        float posY;
        float dirX;
        float dirY;
        float speed;
        float water;
        float sediment;
        TerrainPos gradient1 = new TerrainPos();
        TerrainPos gradient2 = new TerrainPos();

        random.setSeed(NoiseUtil.seed(seedX, seedZ));
        while (iterations-- > 0) {
            dirX = 0;
            dirY = 0;
            sediment = 0;

            gradient1.reset();
            gradient2.reset();

            speed = initialSpeed;
            water = initialWaterVolume;

            posX = nextCoord(map.getSize(), random);
            posY = nextCoord(map.getSize(), random);

            for (int lifetime = 0; lifetime < maxDropletLifetime; lifetime++) {
                int nodeX = (int) posX;
                int nodeY = (int) posY;
                int dropletIndex = nodeY * size + nodeX;
                // Calculate droplet's offset inside the cell (0,0) = at NW node, (1,1) = at SE node
                float cellOffsetX = posX - nodeX;
                float cellOffsetY = posY - nodeY;

                // Calculate droplet's height and direction of flow with bilinear interpolation of surrounding heights
                gradient1.at(cells, size, posX, posY);

                // Update the droplet's direction and position (move position 1 unit regardless of speed)
                dirX = (dirX * inertia - gradient1.gradientX * (1 - inertia));
                dirY = (dirY * inertia - gradient1.gradientY * (1 - inertia));

                // Normalize direction
                float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                if (Float.isNaN(len)) {
                    len = 0;
                }

                if (len != 0) {
                    dirX /= len;
                    dirY /= len;
                }

                posX += dirX;
                posY += dirY;

                // Stop simulating droplet if it's not moving or has flowed over edge of map
                if ((dirX == 0 && dirY == 0) || posX < 0 || posX >= size - 1 || posY < 0 || posY >= size - 1) {
                    break;
                }

                // Find the droplet's new height and calculate the deltaHeight
                float newHeight = gradient2.at(cells, size, posX, posY).height;
                float deltaHeight = newHeight - gradient1.height;

                // Calculate the droplet's sediment capacity (higher when moving fast down a slope and contains lots of water)
                float sedimentCapacity = Math.max(-deltaHeight * speed * water * sedimentCapacityFactor, minSedimentCapacity);

                // If carrying more sediment than capacity, or if flowing uphill:
                if (sediment > sedimentCapacity || deltaHeight > 0) {
                    // If moving uphill (deltaHeight > 0) try fill up to the current height, otherwise deposit a fraction of the excess sediment
                    float amountToDeposit = (deltaHeight > 0) ? Math.min(deltaHeight, sediment) : (sediment - sedimentCapacity) * depositSpeed;
                    sediment -= amountToDeposit;

                    // Add the sediment to the four nodes of the current cell using bilinear interpolation
                    // Deposition is not distributed over a radius (like erosion) so that it can fill small pits
                    deposit(cells[dropletIndex], amountToDeposit * (1 - cellOffsetX) * (1 - cellOffsetY));
                    deposit(cells[dropletIndex + 1], amountToDeposit * cellOffsetX * (1 - cellOffsetY));
                    deposit(cells[dropletIndex + size], amountToDeposit * (1 - cellOffsetX) * cellOffsetY);
                    deposit(cells[dropletIndex + size + 1], amountToDeposit * cellOffsetX * cellOffsetY);
                } else {
                    // Erode a fraction of the droplet's current carry capacity.
                    // Clamp the erosion to the change in height so that it doesn't dig a hole in the terrain behind the droplet
                    float amountToErode = Math.min((sedimentCapacity - sediment) * erodeSpeed, -deltaHeight);

                    // Use erosion brush to erode from all nodes inside the droplet's erosion radius
                    for (int brushPointIndex = 0; brushPointIndex < erosionBrushIndices[dropletIndex].length; brushPointIndex++) {
                        int nodeIndex = erosionBrushIndices[dropletIndex][brushPointIndex];
                        Cell cell = cells[nodeIndex];
                        float brushWeight = erosionBrushWeights[dropletIndex][brushPointIndex];
                        float weighedErodeAmount = amountToErode * brushWeight;
                        float deltaSediment = (cell.value < weighedErodeAmount) ? cell.value : weighedErodeAmount;
                        erode(cell, deltaSediment);
                        sediment += deltaSediment;
                    }
                }

                // Update droplet's speed and water content
                speed = (float) Math.sqrt(speed * speed + deltaHeight * gravity);
                water *= (1 - evaporateSpeed);

                if (Float.isNaN(speed)) {
                    speed = 0;
                }
            }
        }
    }

    private void initBrushes(int size, int radius) {
        int[] xOffsets = new int[radius * radius * 4];
        int[] yOffsets = new int[radius * radius * 4];
        float[] weights = new float[radius * radius * 4];
        float weightSum = 0;
        int addIndex = 0;

        for (int i = 0; i < erosionBrushIndices.length; i++) {
            int centreX = i % size;
            int centreY = i / size;

            if (centreY <= radius || centreY >= size - radius || centreX <= radius + 1 || centreX >= size - radius) {
                weightSum = 0;
                addIndex = 0;
                for (int y = -radius; y <= radius; y++) {
                    for (int x = -radius; x <= radius; x++) {
                        float sqrDst = x * x + y * y;
                        if (sqrDst < radius * radius) {
                            int coordX = centreX + x;
                            int coordY = centreY + y;

                            if (coordX >= 0 && coordX < size && coordY >= 0 && coordY < size) {
                                float weight = 1 - (float) Math.sqrt(sqrDst) / radius;
                                weightSum += weight;
                                weights[addIndex] = weight;
                                xOffsets[addIndex] = x;
                                yOffsets[addIndex] = y;
                                addIndex++;
                            }
                        }
                    }
                }
            }

            int numEntries = addIndex;
            erosionBrushIndices[i] = new int[numEntries];
            erosionBrushWeights[i] = new float[numEntries];

            for (int j = 0; j < numEntries; j++) {
                erosionBrushIndices[i][j] = (yOffsets[j] + centreY) * size + xOffsets[j] + centreX;
                erosionBrushWeights[i][j] = weights[j] / weightSum;
            }
        }
    }

    private void deposit(Cell cell, float amount) {
        if (!cell.erosionMask) {
            float change = modifier.modify(cell, amount);
            cell.value += change;
            cell.sediment += change;
        }
    }

    private void erode(Cell cell, float amount) {
        if (!cell.erosionMask) {
            float change = modifier.modify(cell, amount);
            cell.value -= change;
            cell.erosion -= change;
        }
    }

    private static class TerrainPos {
        private float height;
        private float gradientX;
        private float gradientY;

        private TerrainPos at(Cell[] nodes, int mapSize, float posX, float posY) {
            int coordX = (int) posX;
            int coordY = (int) posY;

            // Calculate droplet's offset inside the cell (0,0) = at NW node, (1,1) = at SE node
            float x = posX - coordX;
            float y = posY - coordY;

            // Calculate heights of the four nodes of the droplet's cell
            int nodeIndexNW = coordY * mapSize + coordX;
            float heightNW = nodes[nodeIndexNW].value;
            float heightNE = nodes[nodeIndexNW + 1].value;
            float heightSW = nodes[nodeIndexNW + mapSize].value;
            float heightSE = nodes[nodeIndexNW + mapSize + 1].value;

            // Calculate droplet's direction of flow with bilinear interpolation of height difference along the edges
            this.gradientX = (heightNE - heightNW) * (1 - y) + (heightSE - heightSW) * y;
            this.gradientY = (heightSW - heightNW) * (1 - x) + (heightSE - heightNE) * x;
            // Calculate height with bilinear interpolation of the heights of the nodes of the cell
            this.height = heightNW * (1 - x) * (1 - y) + heightNE * x * (1 - y) + heightSW * (1 - x) * y + heightSE * x * y;
            return this;
        }

        private void reset() {
            height = 0;
            gradientX = 0;
            gradientY = 0;
        }
    }

    private static class Factory implements IntFunction<Erosion> {

        private final Modifier modifier;
        private final FilterSettings.Erosion settings;

        private Factory(GeneratorContext context) {
            this.settings = context.settings.filters.erosion.copy();
            this.modifier = Modifier.range(context.levels.ground, context.levels.ground(15));
        }

        @Override
        public Erosion apply(int size) {
            return new Erosion(size, settings, modifier);
        }
    }

    public static IntFunction<Erosion> factory(GeneratorContext context) {
        return new Factory(context);
    }
}
