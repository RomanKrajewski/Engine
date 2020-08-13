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

package com.terraforged.world.terrain.populator;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Module;
import com.terraforged.world.terrain.Terrain;

public class VScalePopulator extends TerrainPopulator {

    private final float baseScale;
    private final float varianceScale;

    public VScalePopulator(Terrain type, Module base, Module variance, float baseScale, float varianceScale) {
        super(type, base, variance);
        this.baseScale = baseScale;
        this.varianceScale = varianceScale;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float base = this.base.getValue(x, z) * baseScale;
        float variance = this.variance.getValue(x, z) * varianceScale;

        cell.value = base + variance;

        if (cell.value < 0) {
            cell.value = 0;
        } else if (cell.value > 1) {
            cell.value = 1;
        }

        cell.terrain = type;
    }
}
