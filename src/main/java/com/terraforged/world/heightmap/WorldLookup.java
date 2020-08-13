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

package com.terraforged.world.heightmap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.tile.Tile;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.core.tile.gen.TileCache;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.WorldGeneratorFactory;

public class WorldLookup {

    private final float waterLevel;
    private final float beachLevel;
    private final TileCache cache;
    private final Heightmap heightmap;
    private final GeneratorContext context;

    public WorldLookup(WorldGeneratorFactory factory, GeneratorContext context) {
        this.context = context;
        this.cache = context.cache;
        this.heightmap = factory.getHeightmap();
        this.waterLevel = context.levels.water;
        this.beachLevel = context.levels.water(5);
    }

    public Resource<Cell> get(int x, int z) {
        ChunkReader chunk = cache.getChunk(x >> 4, z >> 4);
        Resource<Cell> cell = Cell.pooled();
        cell.get().copy(chunk.getCell(x & 15, z & 15));
        return cell;
    }

    public Resource<Cell> getCell(int x, int z) {
        return getCell(x, z, false);
    }

    public Resource<Cell> getCell(int x, int z, boolean load) {
        Resource<Cell> resource = Cell.pooled();
        applyCell(resource.get(), x, z, load);
        return resource;
    }

    public void applyCell(Cell cell, int x, int z) {
        applyCell(cell, x, z, false);
    }

    public void applyCell(Cell cell, int x, int z, boolean load) {
        if (load && computeAccurate(cell, x, z)) {
            return;
        }

        if (!computeCached(cell, x, z)) {
            compute(cell, x, z);
        }
    }

    private boolean computeAccurate(Cell cell, int x, int z) {
        int rx = cache.chunkToRegion(x >> 4);
        int rz = cache.chunkToRegion(z >> 4);
        Tile tile = cache.getRegion(rx, rz);
        Cell c = tile.getCell(x, z);
        if (c != null) {
            cell.copy(c);
        }
        return cell.terrain != null;
    }

    private boolean computeCached(Cell cell, int x, int z) {
        int rx = cache.chunkToRegion(x >> 4);
        int rz = cache.chunkToRegion(z >> 4);
        Tile tile = cache.getIfPresent(rx, rz);
        if (tile != null) {
            Cell c = tile.getCell(x, z);
            if (c != null) {
                cell.copy(c);
            }
            return cell.terrain != null;
        }
        return false;
    }

    private void compute(Cell cell, int x, int z) {
        heightmap.apply(cell, x, z);

        // approximation - actual beaches depend on steepness but that's too expensive to calculate
        if (cell.terrain == context.terrain.coast && cell.value > waterLevel && cell.value <= beachLevel) {
            cell.terrain = context.terrain.beach;
        }
    }
}
