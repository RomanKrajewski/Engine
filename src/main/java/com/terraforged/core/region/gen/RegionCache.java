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
import com.terraforged.core.concurrent.cache.Cache;
import com.terraforged.core.concurrent.cache.CacheEntry;
import com.terraforged.core.region.Region;
import com.terraforged.core.region.chunk.ChunkReader;

import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

public class RegionCache implements Disposable.Listener<Region> {

    private final boolean queuing;
    private final RegionGenerator generator;
    private final Cache<CacheEntry<Region>> cache;
    private final LongFunction<CacheEntry<Region>> syncGetter;
    private final LongFunction<CacheEntry<Region>> asyncGetter;

    public RegionCache(boolean queueNeighbours, RegionGenerator generator) {
        this.generator = generator;
        this.syncGetter = syncGetter();
        this.asyncGetter = asyncGetter();
        this.queuing = queueNeighbours;
        this.cache = new Cache<>(60, 30, TimeUnit.SECONDS);
        generator.setListener(this);
    }

    @Override
    public void onDispose(Region region) {
        cache.remove(region.getRegionId());
    }

    public int chunkToRegion(int coord) {
        return generator.chunkToRegion(coord);
    }

    public ChunkReader getChunk(int chunkX, int chunkZ) {
        int regionX = generator.chunkToRegion(chunkX);
        int regionZ = generator.chunkToRegion(chunkZ);
        long regionId = Region.getRegionId(regionX, regionZ);
        return cache.map(regionId, syncGetter, entry -> entry.get().getChunk(chunkX, chunkZ));
    }

    public Region getRegion(int regionX, int regionZ) {
        Region region = getEntry(regionX, regionZ).get();
        if (queuing) {
            queueNeighbours(regionX, regionZ);
        }
        return region;
    }

    public Region getIfPresent(int regionX, int regionZ) {
        CacheEntry<Region> entry = cache.get(Region.getRegionId(regionX, regionZ));
        if (entry == null || !entry.isDone()) {
            return null;
        }
        return entry.get();
    }

    public CacheEntry<Region> getEntry(int regionX, int regionZ) {
        return cache.computeIfAbsent(Region.getRegionId(regionX, regionZ), syncGetter);
    }

    public CacheEntry<Region> queueRegion(int regionX, int regionZ) {
        return cache.computeIfAbsent(Region.getRegionId(regionX, regionZ), asyncGetter);
    }

    private LongFunction<CacheEntry<Region>> syncGetter() {
        return id -> generator.getSync((int) id, (int) (id >> 32));
    }

    private LongFunction<CacheEntry<Region>> asyncGetter() {
        return id -> generator.getAsync((int) id, (int) (id >> 32));
    }

    private void queueNeighbours(int rx, int rz) {
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = 0; dx <= 1; dx++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                queueRegion(rx + dx, rz + dz);
            }
        }
    }
}
