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

package com.terraforged.core.tile.gen;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;
import com.terraforged.core.tile.Tile;

public class TileGeneratorBatched extends TileGenerator {

    public TileGeneratorBatched(Builder builder) {
        super(builder);
    }

    @Override
    public Tile generateRegion(int regionX, int regionZ) {
        Tile tile = createEmptyRegion(regionX, regionZ);
        try (Resource<Batcher> batcher = threadPool.batcher()) {
            tile.generateArea(generator.getHeightmap(), batcher.get(), batchSize);
        }
        postProcess(tile);
        return tile;
    }

    @Override
    public Tile generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        Tile tile = createEmptyRegion(0, 0);
        try (Resource<Batcher> batcher = threadPool.batcher()) {
            tile.generateArea(generator.getHeightmap(), batcher.get(), batchSize, centerX, centerZ, zoom);
        }
        postProcess(tile, filter);
        return tile;
    }
}
