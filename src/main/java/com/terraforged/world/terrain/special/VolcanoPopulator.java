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

package com.terraforged.world.terrain.special;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.heightmap.RegionConfig;
import com.terraforged.world.terrain.Terrain;
import com.terraforged.world.terrain.populator.TerrainPopulator;
import com.terraforged.world.terrain.Terrains;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.func.EdgeFunc;

public class VolcanoPopulator extends TerrainPopulator {

    private static final float throat_value = 0.925F;

    private final Module cone;
    private final Module height;
    private final Module lowlands;
    private final float inversionPoint;
    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;
    private final float bias;

    private final Terrain inner;
    private final Terrain outer;

    public VolcanoPopulator(Seed seed, RegionConfig region, Levels levels, Terrains terrains) {
        super(terrains.volcano, Source.ZERO, Source.ZERO);
        float midpoint = 0.3F;
        float range = 0.3F;

        Module heightNoise = Source.perlin(seed.next(), 2, 1).map(0.45, 0.65);

        this.height = Source.cellNoise(region.seed, region.scale, heightNoise)
                .warp(region.warpX, region.warpZ, region.warpStrength);

        this.cone = Source.cellEdge(region.seed, region.scale, EdgeFunc.DISTANCE_2_DIV).invert()
                .warp(region.warpX, region.warpZ, region.warpStrength)
                .powCurve(11)
                .clamp(0.475, 1)
                .map(0, 1)
                .grad(0, 0.5, 0.5)
                .warp(seed.next(), 15, 2, 10)
                .scale(height);

        this.lowlands = Source.ridge(seed.next(), 150, 3)
                .warp(seed.next(), 30, 1, 30)
                .scale(0.1);

        this.inversionPoint = 0.94F;
        this.blendLower = midpoint - (range / 2F);
        this.blendUpper = blendLower + range;
        this.blendRange = blendUpper - blendLower;
        this.outer = terrains.volcano;
        this.inner = terrains.volcanoPipe;
        this.bias = levels.ground;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float value = cone.getValue(x, z);
        float limit = height.getValue(x, z);
        float maxHeight = limit * inversionPoint;

        // as value passes the inversion point we start calculating the inner-cone of the volcano
        if (value > maxHeight) {
            // modifies the steepness of the volcano inner-cone (larger == steeper)
            float steepnessModifier = 1F;

            // as alpha approaches 1.0, position is closer to center of volcano
            float delta = (value - maxHeight) * steepnessModifier;
            float range = (limit - maxHeight);
            float alpha = delta / range;

            // calculate height inside volcano
            if (alpha > throat_value) {
                cell.terrain = inner;
            }

            value = maxHeight - ((maxHeight / 5F) * alpha);
        } else if (value < blendLower) {
            value += lowlands.getValue(x, z);
            cell.terrain = outer;
        } else if (value < blendUpper) {
            float alpha = 1 - ((value - blendLower) / blendRange);
            value += (lowlands.getValue(x, z) * alpha);
            cell.terrain = outer;
        }

        cell.value = bias + value;
    }

    @Override
    public void tag(Cell cell, float x, float z) {
        float value = cone.getValue(x, z);
        float limit = height.getValue(x, z);
        float maxHeight = limit * inversionPoint;
        if (value > maxHeight) {
            float steepnessModifier = 1;

            // as alpha approaches 1.0, position is closer to center of volcano
            float delta = (value - maxHeight) * steepnessModifier;
            float range = (limit - maxHeight);
            float alpha = delta / range;

            // calculate height inside volcano
            if (alpha > throat_value) {
                cell.terrain = inner;
            } else {
                cell.terrain = outer;
            }
        } else {
            cell.terrain = outer;
        }
    }
}
