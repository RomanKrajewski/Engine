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

package com.terraforged.world;

import com.terraforged.core.filter.BeachDetect;
import com.terraforged.core.filter.Erosion;
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.filter.Smoothing;
import com.terraforged.core.filter.Steepness;
import com.terraforged.core.settings.FilterSettings;
import com.terraforged.core.tile.Size;
import com.terraforged.core.tile.Tile;

import java.util.function.IntFunction;

public class WorldFilters {

    private final Smoothing smoothing;
    private final Steepness steepness;
    private final BeachDetect beach;
    private final FilterSettings settings;
    private final IntFunction<Erosion> erosionFactory;
    private final Object lock = new Object();

    private Erosion erosion = null;

    public WorldFilters(GeneratorContext context) {
        context = context.copy();
        this.settings = context.settings.filters;
        this.beach = new BeachDetect(context);
        this.smoothing = new Smoothing(context.settings, context.levels);
        this.steepness = new Steepness(1, 10F, context.terrain, context.levels);
        this.erosionFactory = Erosion.factory(context);
    }

    public void apply(Tile tile, boolean optionalFilters) {
        Filterable map = tile.filterable();

        if (optionalFilters) {
            getErosion(map.getSize()).apply(map, tile.getRegionX(), tile.getRegionZ(), settings.erosion.iterations);
            smoothing.apply(map, tile.getRegionX(), tile.getRegionZ(), settings.smoothing.iterations);
        }

        steepness.apply(map, tile.getRegionX(), tile.getRegionZ(), 1);
        beach.apply(map, tile.getRegionX(), tile.getRegionZ(), 1);
    }

    private Erosion getErosion(Size size) {
        synchronized (lock) {
            if (erosion == null || erosion.getSize() != size.total) {
                erosion = erosionFactory.apply(size.total);
            }
        }
        return erosion;
    }
}
