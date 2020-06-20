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
import com.terraforged.core.concurrent.cache.Cache;
import com.terraforged.core.concurrent.cache.CacheEntry;
import com.terraforged.core.tile.Tile;
import com.terraforged.core.tile.chunk.ChunkReader;

import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

public class TileCache implements Disposable.Listener<Tile> {

    private final boolean queuing;
    private final TileGenerator generator;
    private final Cache<CacheEntry<Tile>> cache;
    private final LongFunction<CacheEntry<Tile>> syncGetter;
    private final LongFunction<CacheEntry<Tile>> asyncGetter;

    public TileCache(boolean queueNeighbours, TileGenerator generator) {
        this.generator = generator;
        this.syncGetter = syncGetter();
        this.asyncGetter = asyncGetter();
        this.queuing = queueNeighbours;
        this.cache = new Cache<>(60, 30, TimeUnit.SECONDS);
        generator.setListener(this);
    }

    @Override
    public void onDispose(Tile tile) {
        cache.remove(tile.getRegionId());
    }

    public int chunkToRegion(int coord) {
        return generator.chunkToRegion(coord);
    }

    public ChunkReader getChunk(int chunkX, int chunkZ) {
        int regionX = generator.chunkToRegion(chunkX);
        int regionZ = generator.chunkToRegion(chunkZ);
        long regionId = Tile.getRegionId(regionX, regionZ);
        return cache.map(regionId, syncGetter, entry -> entry.get().getChunk(chunkX, chunkZ));
    }

    public Tile getRegion(int regionX, int regionZ) {
        Tile tile = getEntry(regionX, regionZ).get();
        if (queuing) {
            queueNeighbours(regionX, regionZ);
        }
        return tile;
    }

    public Tile getIfPresent(int regionX, int regionZ) {
        CacheEntry<Tile> entry = cache.get(Tile.getRegionId(regionX, regionZ));
        if (entry == null || !entry.isDone()) {
            return null;
        }
        return entry.get();
    }

    public CacheEntry<Tile> getEntry(int regionX, int regionZ) {
        return cache.computeIfAbsent(Tile.getRegionId(regionX, regionZ), syncGetter);
    }

    public CacheEntry<Tile> queueRegion(int regionX, int regionZ) {
        return cache.computeIfAbsent(Tile.getRegionId(regionX, regionZ), asyncGetter);
    }

    private LongFunction<CacheEntry<Tile>> syncGetter() {
        return id -> generator.getSync((int) id, (int) (id >> 32));
    }

    private LongFunction<CacheEntry<Tile>> asyncGetter() {
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
