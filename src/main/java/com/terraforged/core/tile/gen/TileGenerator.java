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

package com.terraforged.core.tile.gen;

import com.terraforged.core.concurrent.Disposable;
import com.terraforged.core.concurrent.cache.CacheEntry;
import com.terraforged.core.concurrent.thread.ThreadPool;
import com.terraforged.core.tile.Tile;
import com.terraforged.world.WorldGenerator;
import com.terraforged.world.WorldGeneratorFactory;

public class TileGenerator {

    protected final int factor;
    protected final int border;
    protected final int batchSize;
    protected final ThreadPool threadPool;
    protected final WorldGenerator generator;
    private final TileResources resources = new TileResources();

    private Disposable.Listener<Tile> listener = r -> {
    };

    protected TileGenerator(Builder builder) {
        this.factor = builder.factor;
        this.border = builder.border;
        this.batchSize = builder.batchSize;
        this.threadPool = builder.threadPool;
        this.generator = builder.factory.get();
    }

    protected void setListener(Disposable.Listener<Tile> listener) {
        this.listener = listener;
    }

    public int chunkToRegion(int i) {
        return i >> factor;
    }

    public TileCache toCache() {
        return toCache(true);
    }

    public TileCache toCache(boolean queueNeighbours) {
        return new TileCache(queueNeighbours, this);
    }

    public CacheEntry<Tile> getSync(int regionX, int regionZ) {
        return CacheEntry.supply(new CallableTile(regionX, regionZ, this));
    }

    public CacheEntry<Tile> getSync(int regionX, int regionZ, float zoom, boolean filter) {
        return CacheEntry.supply(new CallableZoomTile(regionX, regionZ, zoom, filter, this));
    }

    public CacheEntry<Tile> getAsync(int regionX, int regionZ) {
        return CacheEntry.supplyAsync(new CallableTile(regionX, regionZ, this), threadPool);
    }

    public CacheEntry<Tile> getAsync(float centerX, float centerZ, float zoom, boolean filter) {
        return CacheEntry.supplyAsync(new CallableZoomTile(centerX, centerZ, zoom, filter, this), threadPool);
    }

    public Tile generateRegion(int regionX, int regionZ) {
        Tile tile = createEmptyRegion(regionX, regionZ);
        tile.generate(generator.getHeightmap());
        postProcess(tile);
        return tile;
    }

    public Tile generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        Tile tile = createEmptyRegion(0, 0);
        tile.generate(generator.getHeightmap(), centerX, centerZ, zoom);
        postProcess(tile, centerX, centerZ, zoom, filter);
        return tile;
    }

    protected Tile createEmptyRegion(int regionX, int regionZ) {
        return new Tile(regionX, regionZ, factor, border, resources, listener);
    }

    protected void postProcess(Tile tile) {
        generator.getFilters().apply(tile, true);
        tile.decorate(generator.getDecorators().getDecorators());
    }

    protected void postProcess(Tile tile, float centerX, float centerZ, float zoom, boolean filter) {
        generator.getFilters().apply(tile, filter);
        tile.decorateZoom(generator.getDecorators().getDecorators(), centerX, centerZ, zoom);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int factor = 0;
        private int border = 0;
        private int batchSize = 0;
        private ThreadPool threadPool;
        private WorldGeneratorFactory factory;

        public Builder size(int factor, int border) {
            return factor(factor).border(border);
        }

        public Builder factor(int factor) {
            this.factor = factor;
            return this;
        }

        public Builder border(int border) {
            this.border = border;
            return this;
        }

        public Builder pool(ThreadPool threadPool) {
            this.threadPool = threadPool;
            return this;
        }

        public Builder factory(WorldGeneratorFactory factory) {
            this.factory = factory;
            return this;
        }

        public Builder batch(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public TileGenerator build() {
            if (threadPool.supportsBatching() && batchSize > 1) {
                return new TileGeneratorBatched(this);
            }
            return new TileGenerator(this);
        }
    }
}
