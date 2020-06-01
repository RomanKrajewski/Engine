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

package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;

public class  Steepness implements Filter, Filter.Visitor {

    private final int radius;
    private final float scaler;
    private final float waterLevel;
    private final float maxBeachLevel;
    private final Terrains terrains;

    public Steepness(int radius, float scaler, Terrains terrains, Levels levels) {
        this.radius = radius;
        this.scaler = scaler;
        this.terrains = terrains;
        this.waterLevel = levels.water;
        this.maxBeachLevel = levels.water(6);
    }

    @Override
    public void apply(Filterable cellMap, int seedX, int seedZ, int iterations) {
        iterate(cellMap, this);
    }

    @Override
    public void visit(Filterable cellMap, Cell cell, int cx, int cz) {
        float totalHeightDif = 0F;
        for (int dz = -1; dz <= 2; dz++) {
            for (int dx = -1; dx <= 2; dx++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                int x = cx + dx * radius;
                int z = cz + dz * radius;
                Cell neighbour = cellMap.getCellRaw(x, z);
                if (neighbour.isAbsent()) {
                    continue;
                }

                float height = Math.max(neighbour.value, waterLevel);

                totalHeightDif += (Math.abs(cell.value - height) / radius);
            }
        }
        cell.steepness = Math.min(1, totalHeightDif * scaler);
        if (cell.terrain == terrains.coast && cell.steepness < 0.225F && cell.value < maxBeachLevel) {
            cell.terrain = terrains.beach;
        }
    }
}
