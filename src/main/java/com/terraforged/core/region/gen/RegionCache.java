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

import com.terraforged.core.region.Region;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.concurrent.Disposable;
import com.terraforged.core.concurrent.cache.Cache;
import com.terraforged.core.concurrent.cache.CacheEntry;
import me.dags.noise.util.NoiseUtil;

import java.util.concurrent.TimeUnit;

public class RegionCache implements Disposable.Listener<Region> {

    private final boolean queuing;
    private final RegionGenerator generator;
    private final Cache<CacheEntry<Region>> cache;

    public RegionCache(boolean queueNeighbours, RegionGenerator generator) {
        this.queuing = queueNeighbours;
        this.generator = generator.withListener(this);
        this.cache = new Cache<>(60, 30, TimeUnit.SECONDS);
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
        Region region = getRegion(regionX, regionZ);
        return region.getChunk(chunkX, chunkZ);
    }

    public Region getRegion(int regionX, int regionZ) {
        Region region = getEntry(regionX, regionZ).get();
        if (queuing) {
            queueNeighbours(regionX, regionZ);
        }
        return region;
    }

    public Region getIfPresent(int regionX, int regionZ) {
        long id = NoiseUtil.seed(regionX, regionZ);
        CacheEntry<Region> entry = cache.get(id);
        if (entry == null || !entry.isDone()) {
            return null;
        }
        return entry.get();
    }

    public CacheEntry<Region> getEntry(int regionX, int regionZ) {
        long id = NoiseUtil.seed(regionX, regionZ);
        return cache.computeIfAbsent(id, l -> generator.getSync(regionX, regionZ));
    }

    public void queueRegion(int regionX, int regionZ) {
        long id = NoiseUtil.seed(regionX, regionZ);
        cache.computeIfAbsent(id, l -> generator.getAsync(regionX, regionZ));
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
