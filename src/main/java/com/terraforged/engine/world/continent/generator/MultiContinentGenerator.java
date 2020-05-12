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

package com.terraforged.engine.world.continent.generator;

import com.terraforged.engine.core.cell.Cell;
import com.terraforged.engine.core.settings.WorldSettings;
import com.terraforged.engine.core.NumConstants;
import com.terraforged.engine.core.Seed;
import com.terraforged.engine.world.continent.MutableVeci;
import me.dags.noise.func.DistanceFunc;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;

public class MultiContinentGenerator extends AbstractContinentGenerator {

    public MultiContinentGenerator(Seed seed, WorldSettings settings) {
        super(seed, settings);
    }

    @Override
    public void apply(Cell cell, float x, float y, float px, float py) {
        int cellX = 0;
        int cellY = 0;
        Vec2f center = null;

        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float edgeDistance = NumConstants.LARGE;
        float edgeDistance2 = NumConstants.LARGE;
        float valueDistance = NumConstants.LARGE;
        DistanceFunc dist = getDistFunc();

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < valueDistance) {
                    valueDistance = distance;
                    cellX = xi;
                    cellY = yi;
                    center = vec;
                }

                if (distance < edgeDistance2) {
                    edgeDistance2 = Math.max(edgeDistance, distance);
                } else {
                    edgeDistance2 = Math.max(edgeDistance, edgeDistance2);
                }

                edgeDistance = Math.min(edgeDistance, distance);
            }
        }


        float edgeValue = edgeValue(edgeDistance, edgeDistance2);
        float shapeNoise = getShape(x, y, edgeValue);
        float continentNoise = continentValue(edgeValue);
        cell.continent = cellValue(seed, cellX, cellY);
        cell.continentEdge = shapeNoise * continentNoise;
        cell.continentEdgeRaw = shapeNoise * edgeValue;
        cell.continentX = (int) ((cellX + center.x) / frequency);
        cell.continentZ = (int) ((cellY + center.y) / frequency);
    }

    @Override
    public void getContinentCenter(float px, float py, MutableVeci pos) {
        int cellX = 0;
        int cellY = 0;
        Vec2f center = null;

        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float valueDistance = NumConstants.LARGE;
        DistanceFunc dist = getDistFunc();

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < valueDistance) {
                    valueDistance = distance;
                    cellX = xi;
                    cellY = yi;
                    center = vec;
                }
            }
        }

        if (center == null) {
            return;
        }

        pos.x = (int) ((cellX + center.x) / frequency);
        pos.z = (int) ((cellY + center.y) / frequency);
    }

    @Override
    public float getEdgeNoise(float x, float y, float px, float py) {
        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float edgeDistance = 999999.0F;
        float edgeDistance2 = 999999.0F;
        DistanceFunc dist = getDistFunc();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < edgeDistance2) {
                    edgeDistance2 = Math.max(edgeDistance, distance);
                } else {
                    edgeDistance2 = Math.max(edgeDistance, edgeDistance2);
                }

                edgeDistance = Math.min(edgeDistance, distance);
            }
        }
        float edgeValue = edgeValue(edgeDistance, edgeDistance2);
        float shapeNoise = getShape(x, y, edgeValue);
        return continentValue(edgeValue) * shapeNoise;
    }
}
