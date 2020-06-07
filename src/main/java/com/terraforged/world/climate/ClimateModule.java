/*
 *   
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

package com.terraforged.world.climate;

import com.terraforged.core.NumConstants;
import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.Settings;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.continent.Continent;
import com.terraforged.world.terrain.Terrains;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.DistanceFunc;
import me.dags.noise.func.EdgeFunc;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;

public class ClimateModule {

    private static final float MOISTURE_SIZE = 2.5F;

    private final int seed;

    private final float edgeClamp;
    private final float edgeScale;
    private final float biomeFreq;
    private final float warpStrength;

    private final Module warpX;
    private final Module warpZ;
    private final Module moisture;
    private final Module temperature;
    private final Continent continent;
    private final Terrains terrains;

    public ClimateModule(Continent continent, GeneratorContext context) {
        Seed seed = context.seed;
        Settings settings = context.settings;

        int biomeSize = settings.climate.biomeShape.biomeSize;
        float tempScaler = settings.climate.temperature.scale;
        float moistScaler = settings.climate.moisture.scale * MOISTURE_SIZE;

        float biomeFreq = 1F / biomeSize;
        float moistureSize = moistScaler * biomeSize;
        float temperatureSize = tempScaler * biomeSize;
        int moistScale = NoiseUtil.round(moistureSize * biomeFreq);
        int tempScale = NoiseUtil.round(temperatureSize * biomeFreq);
        int warpScale = settings.climate.biomeShape.biomeWarpScale;

        this.terrains = context.terrain;
        this.continent = continent;
        this.seed = seed.next();
        this.edgeClamp = 1F;
        this.edgeScale = 1 / edgeClamp;
        this.biomeFreq = 1F / biomeSize;
        this.warpStrength = settings.climate.biomeShape.biomeWarpStrength;
        this.warpX = Source.simplex(seed.next(), warpScale, 2).bias(-0.5);
        this.warpZ = Source.simplex(seed.next(), warpScale, 2).bias(-0.5);

        Module moisture = new Moisture(seed.next(), moistScale, settings.climate.moisture.falloff);
        this.moisture = settings.climate.moisture.apply(moisture)
                .warp(seed.next(), moistScale / 2, 1, moistScale / 4D)
                .warp(seed.next(), moistScale / 6, 2, moistScale / 12D);

        Module temperature = new Temperature(1F / tempScale, settings.climate.temperature.falloff);
        this.temperature = settings.climate.temperature.apply(temperature)
                .warp(seed.next(), tempScale * 4, 2, tempScale * 4)
                .warp(seed.next(), tempScale, 1, tempScale);
    }

    public void apply(Cell cell, float x, float y) {
        apply(cell, x, y, true);
    }

    public void apply(Cell cell, float x, float y, boolean mask) {
        float ox = warpX.getValue(x, y) * warpStrength;
        float oz = warpZ.getValue(x, y) * warpStrength;

        x += ox;
        y += oz;

        x *= biomeFreq;
        y *= biomeFreq;

        int cellX = 0;
        int cellY = 0;

        Vec2f vec2f = null;
        int xr = NoiseUtil.round(x);
        int yr = NoiseUtil.round(y);
        float edgeDistance = NumConstants.LARGE;
        float edgeDistance2 = NumConstants.LARGE;
        float valueDistance = NumConstants.LARGE;
        DistanceFunc dist = DistanceFunc.EUCLIDEAN;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - x + vec.x;
                float vecY = yi - y + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < valueDistance) {
                    valueDistance = distance;
                    vec2f = vec;
                    cellX = xi;
                    cellY = yi;
                }

                if (distance < edgeDistance2) {
                    edgeDistance2 = Math.max(edgeDistance, distance);
                } else {
                    edgeDistance2 = Math.max(edgeDistance, edgeDistance2);
                }

                edgeDistance = Math.min(edgeDistance, distance);
            }
        }

        float biomeX = cellX + vec2f.x;
        float biomeY = cellY + vec2f.y;

        cell.biome = cellValue(seed, cellX, cellY);
        cell.moisture = moisture.getValue(biomeX, biomeY);
        cell.temperature = temperature.getValue(biomeX, biomeY);

        int posX = (int) (biomeX / biomeFreq);
        int posZ = (int) (biomeY / biomeFreq);
        float continentEdge = continent.getEdgeNoise(posX, posZ);

        if (mask) {
            cell.biomeEdge = edgeValue(edgeDistance, edgeDistance2);
            modifyTerrain(cell, continentEdge);
        }

        modifyMoisture(cell, continentEdge);

        cell.biomeType = BiomeType.get(cell.temperature, cell.moisture);
    }

    private void modifyMoisture(Cell cell, float continentEdge) {
        if (continentEdge < 0.7F) {
            float alpha = (0.7F - continentEdge) / 0.3F;
            float multiplier = 1F + (alpha * 0.3F);
            cell.moisture = NoiseUtil.clamp(cell.moisture * multiplier, 0, 1F);
        } else {
            float alpha = (continentEdge - 0.7F) / 0.3F;
            float multiplier = 1F - (alpha * 0.3F);
            cell.moisture *= multiplier;
        }
    }

    private void modifyTerrain(Cell cell, float continentEdge) {
        if (continentEdge <= 0.65F) {
            cell.terrain = terrains.coast;
        }
    }

    private float cellValue(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1, 1, 2);
    }

    private float edgeValue(float distance, float distance2) {
        EdgeFunc edge = EdgeFunc.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        value = 1 - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        if (true) {
            return value;
        }
        if (value > edgeClamp) {
            return 1F;
        }
        return value * edgeScale;
    }
}
