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

package com.terraforged.world.rivermap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.cache.Cache;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.rivermap.gen.RiverGenerator;

import java.util.concurrent.TimeUnit;

public class RiverCache {

    private final RiverGenerator generator;
    private final Cache<Rivermap> cache = new Cache<>(5, 1, TimeUnit.MINUTES);

    public RiverCache(Heightmap heightmap, GeneratorContext context) {
        this.generator = new RiverGenerator(heightmap, context);
    }

    public Rivermap getRivers(Cell cell) {
        return getRivers(cell.continentX, cell.continentZ);
    }

    public Rivermap getRivers(int x, int z) {
        return cache.computeIfAbsent(NoiseUtil.seed(x, z), id -> generator.compute(x, z, id));
    }
}
