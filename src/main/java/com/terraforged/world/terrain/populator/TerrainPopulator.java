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

package com.terraforged.world.terrain.populator;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.settings.TerrainSettings;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.world.terrain.Terrain;

public class TerrainPopulator implements Populator {

    protected final Terrain type;
    protected final Module base;
    protected final Module variance;

    public TerrainPopulator(Terrain type, Module base, Module variance) {
        this.type = type;
        this.base = base;
        this.variance = variance;
    }

    public Module getVariance() {
        return variance;
    }

    public Terrain getType() {
        return type;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float base = this.base.getValue(x, z);
        float variance = this.variance.getValue(x, z);

        cell.value = base + variance;

        if (cell.value < 0) {
            cell.value = 0;
        } else if (cell.value > 1) {
            cell.value = 1;
        }

        cell.terrain = type;
    }

    public static Module clamp(Module module) {
        if (module.minValue() < 0 || module.maxValue() > 1) {
            return module.clamp(0, 1);
        }
        return module;
    }

    public static TerrainPopulator of(Terrain type, Module variance) {
        return new TerrainPopulator(type, Source.ZERO, variance);
    }

    public static TerrainPopulator of(Terrain type, Module base, Module variance, TerrainSettings.Terrain settings) {
        // no horizontal scaling to be applied
        if (settings.horizontalScale == 1) {
            // no vertical scaling
            if (settings.verticalScale == 1 && settings.baseScale == 1) {
                return new TerrainPopulator(type, base, variance);
            }

            // vertical scaling only
            return new VScalePopulator(type, base, variance, settings.baseScale, settings.verticalScale);
        }

        // horizontal scaling only
        if (settings.verticalScale == 1 && settings.baseScale == 1) {
            return new HScalePopulator(type, base, variance, settings.horizontalScale);
        }

        // horizontal & vertical scaling
        return new HVScalePopulator(type, base, variance, settings.baseScale, settings.verticalScale, settings.horizontalScale);
    }
}
