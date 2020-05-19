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

package com.terraforged.core.region.gen;

import com.terraforged.core.concurrent.Disposable;
import com.terraforged.core.concurrent.cache.CacheEntry;
import com.terraforged.core.concurrent.pool.ThreadPool;
import com.terraforged.core.region.Region;
import com.terraforged.world.WorldGenerator;
import com.terraforged.world.WorldGeneratorFactory;

public class RegionGenerator {

    protected final int factor;
    protected final int border;
    protected final int batchSize;
    protected final ThreadPool threadPool;
    protected final WorldGenerator generator;
    protected final Disposable.Listener<Region> disposalListener;

    protected RegionGenerator(Builder builder) {
        this.factor = builder.factor;
        this.border = builder.border;
        this.batchSize = builder.batchSize;
        this.threadPool = builder.threadPool;
        this.disposalListener = region -> {};
        this.generator = builder.factory.get();
    }

    protected RegionGenerator(RegionGenerator from, Disposable.Listener<Region> listener) {
        this.factor = from.factor;
        this.border = from.border;
        this.batchSize = from.batchSize;
        this.threadPool = from.threadPool;
        this.disposalListener = listener;
        this.generator = from.generator;
    }

    public int chunkToRegion(int i) {
        return i >> factor;
    }

    public RegionGenerator withListener(Disposable.Listener<Region> listener) {
        return new RegionGenerator(this, listener);
    }

    public RegionCache toCache() {
        return toCache(true);
    }

    public RegionCache toCache(boolean queueNeighbours) {
        return new RegionCache(queueNeighbours, this);
    }

    public CacheEntry<Region> getSync(int regionX, int regionZ) {
        return CacheEntry.supply(new CallableRegion(regionX, regionZ, this));
    }

    public CacheEntry<Region> getSync(int regionX, int regionZ, float zoom, boolean filter) {
        return CacheEntry.supply(new CallableZoomRegion(regionX, regionZ, zoom, filter, this));
    }

    public CacheEntry<Region> getAsync(int regionX, int regionZ) {
        return CacheEntry.supplyAsync(new CallableRegion(regionX, regionZ, this), threadPool);
    }

    public CacheEntry<Region> getAsync(float centerX, float centerZ, float zoom, boolean filter) {
        return CacheEntry.supplyAsync(new CallableZoomRegion(centerX, centerZ, zoom, filter, this), threadPool);
    }

    public Region generateRegion(int regionX, int regionZ) {
        Region region = new Region(regionX, regionZ, factor, border, disposalListener);
        region.generate(generator.getHeightmap());
        postProcess(region);
        return region;
    }

    public Region generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        Region region = new Region(0, 0, factor, border, disposalListener);
        region.generate(generator.getHeightmap(), centerX, centerZ, zoom);
        postProcess(region, centerX, centerZ, zoom, filter);
        return region;
    }

    protected void postProcess(Region region) {
        generator.getFilters().apply(region);
        region.decorate(generator.getDecorators().getDecorators());
    }

    protected void postProcess(Region region, float centerX, float centerZ, float zoom, boolean filter) {
        if (filter) {
            generator.getFilters().apply(region);
        }
        region.decorateZoom(generator.getDecorators().getDecorators(), centerX, centerZ, zoom);
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

        public RegionGenerator build() {
            if (threadPool.supportsBatching() && batchSize > 1) {
                return new RegionGeneratorBatched(this);
            }
            return new RegionGenerator(this);
        }
    }
}
