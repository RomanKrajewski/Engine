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

package com.terraforged.world.rivermap.lake;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.n2d.util.Vec2f;
import com.terraforged.world.rivermap.river.River;
import com.terraforged.world.terrain.Terrains;
import com.terraforged.world.terrain.populator.TerrainPopulator;

public class Lake extends TerrainPopulator {

    protected final float valley2;
    protected final float lakeDistance2;
    protected final float valleyDistance2;
    protected final float bankAlphaMin;
    protected final float bankAlphaMax;
    protected final float bankAlphaRange;
    private final float depth;
    private final float bankMin;
    private final float bankMax;
    protected final Vec2f center;
    protected final Terrains terrains;

    public Lake(Vec2f center, float radius, float multiplier, LakeConfig config, Terrains terrains) {
        super(terrains.lake, Source.ZERO, Source.ZERO);
        float lake = radius * multiplier;
        float valley = 250 * multiplier;
        this.valley2 = valley * valley;
        this.center = center;
        this.depth = config.depth;
        this.bankMin = config.bankMin;
        this.bankMax = config.bankMax;
        this.bankAlphaMin = config.bankMin;
        this.bankAlphaMax = Math.min(1, bankAlphaMin + 0.275F);
        this.bankAlphaRange = bankAlphaMax - bankAlphaMin;
        this.lakeDistance2 = lake * lake;
        this.valleyDistance2 = valley2 - lakeDistance2;
        this.terrains = terrains;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float distance2 = getDistance2(x, z);
        if (distance2 > valley2) {
            return;
        }

        float bankHeight = getBankHeight(cell);
        if (distance2 > lakeDistance2) {
            if (cell.value < bankHeight) {
                return;
            }

            float valleyAlpha = 1F - ((distance2 - lakeDistance2) / valleyDistance2);
            if (valleyAlpha < 0) {
                valleyAlpha = 0;
            } else if (valleyAlpha > 1) {
                valleyAlpha = 1;
            }

            cell.value = NoiseUtil.lerp(cell.value, bankHeight, valleyAlpha);
            cell.riverMask *= (1 - valleyAlpha);
            return;
        }

        cell.value = Math.min(bankHeight, cell.value);

        if (distance2 < lakeDistance2) {
            float depthAlpha = 1F - (distance2 / lakeDistance2);
            if (depthAlpha < 0) {
                depthAlpha = 0;
            } else if (depthAlpha > 1) {
                depthAlpha = 1;
            }

            float lakeDepth = Math.min(cell.value, depth);
            cell.value = NoiseUtil.lerp(cell.value, lakeDepth, depthAlpha);
            cell.riverMask *= (1 - depthAlpha);
            cell.terrain = terrains.lake;
        }
    }

    public boolean overlaps(float x, float z, float radius2) {
        float dist2 = getDistance2(x, z);
        return dist2 < lakeDistance2 + radius2;
    }

    protected float getDistance2(float x, float z) {
        float dx = center.x - x;
        float dz = center.y - z;
        return (dx * dx + dz * dz);
    }

    protected float getBankHeight(Cell cell) {
        // scale bank height based on elevation of the terrain (higher terrain == taller banks)
        float bankHeightAlpha = NoiseUtil.map(cell.value, bankAlphaMin, bankAlphaMax, bankAlphaRange);
        // lerp between the min and max heights
        return NoiseUtil.lerp(bankMin, bankMax, bankHeightAlpha);
    }
}
