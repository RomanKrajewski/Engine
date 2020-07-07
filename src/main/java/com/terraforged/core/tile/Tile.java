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

package com.terraforged.core.tile;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Disposable;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;
import com.terraforged.core.concurrent.cache.SafeCloseable;
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.tile.chunk.ChunkBatchTask;
import com.terraforged.core.tile.chunk.ChunkGenTask;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.core.tile.chunk.ChunkWriter;
import com.terraforged.core.tile.gen.TileResources;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.rivermap.Rivermap;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Tile implements Disposable, SafeCloseable {

    private final int regionX;
    private final int regionZ;
    private final int chunkX;
    private final int chunkZ;
    private final int blockX;
    private final int blockZ;
    private final int border;
    private final int chunkCount;
    private final Size blockSize;
    private final Size chunkSize;
    private final Cell[] blocks;
    private final GenChunk[] chunks;

    // keeps reference to the pooled resources so they can be released once the region has been disposed
    private final Resource<Cell[]> blockResource;
    private final Resource<GenChunk[]> chunkResource;

    // keeps track of 'open/active' chunks (ie chunks that are being read from)
    private final AtomicInteger active = new AtomicInteger();

    // keeps track of 'disposed' chunks (ie chunks that we do not expect to read from again)
    // once all chunks have been disposed the disposal listener is notified
    private final AtomicInteger disposed = new AtomicInteger();

    // basically the RegionCache
    private final Disposable.Listener<Tile> listener;

    public Tile(int regionX, int regionZ, int size, int borderChunks, TileResources resources, Listener<Tile> listener) {
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.listener = listener;
        this.chunkX = regionX << size;
        this.chunkZ = regionZ << size;
        this.blockX = Size.chunkToBlock(chunkX);
        this.blockZ = Size.chunkToBlock(chunkZ);
        this.border = borderChunks;
        this.chunkSize = Size.chunks(size, borderChunks);
        this.blockSize = Size.blocks(size, borderChunks);
        this.chunkCount = chunkSize.size * chunkSize.size;
        this.blockResource = resources.blocks.get(blockSize.arraySize);
        this.chunkResource = resources.chunks.get(chunkSize.arraySize);
        this.blocks = blockResource.get();
        this.chunks = chunkResource.get();
    }

    /**
     * Called when a chunk from this region has been disposed
     */
    @Override
    public void dispose() {
        // should be called once per chunk (excluding border chunks).
        // once the chunk count has been hit we can dispose
        if (disposed.incrementAndGet() >= chunkCount) {
            listener.onDispose(this);
        }
    }

    /**
     * Called when removed from the region cache
     */
    @Override
    public void close() {
        // only dispose resources if there are no chunks actively being used
        if (active.compareAndSet(0, -1)) {
            if (blockResource.isOpen()) {
                // cells can be reused
                for (Cell cell : blocks) {
                    if (cell != null) {
                        cell.reset();
                    }
                }
                blockResource.close();
            }

            if (chunkResource.isOpen()) {
                // chunks must be null'd
                Arrays.fill(chunks, null);
                chunkResource.close();
            }
        }
    }

    public long getRegionId() {
        return getRegionId(getRegionX(), getRegionZ());
    }

    public int getRegionX() {
        return regionX;
    }

    public int getRegionZ() {
        return regionZ;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public int getOffsetChunks() {
        return border;
    }

    public int getChunkCount() {
        return chunks.length;
    }

    public int getBlockCount() {
        return blocks.length;
    }

    public Size getChunkSize() {
        return chunkSize;
    }

    public Size getBlockSize() {
        return blockSize;
    }

    public Filterable filterable() {
        return new FilterRegion();
    }

    public Cell getCell(int blockX, int blockZ) {
        int relBlockX = blockSize.border + blockSize.mask(blockX);
        int relBlockZ = blockSize.border + blockSize.mask(blockZ);
        int index = blockSize.indexOf(relBlockX, relBlockZ);
        return blocks[index];
    }

    public Cell getRawCell(int blockX, int blockZ) {
        int index = blockSize.indexOf(blockX, blockZ);
        return blocks[index];
    }

    public ChunkWriter getChunkWriter(int chunkX, int chunkZ) {
        int index = chunkSize.indexOf(chunkX, chunkZ);
        return computeChunk(index, chunkX, chunkZ);
    }

    public ChunkReader getChunk(int chunkX, int chunkZ) {
        int relChunkX = chunkSize.border + chunkSize.mask(chunkX);
        int relChunkZ = chunkSize.border + chunkSize.mask(chunkZ);
        int index = chunkSize.indexOf(relChunkX, relChunkZ);
        return chunks[index].open();
    }

    public void generate(Consumer<ChunkWriter> consumer) {
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                consumer.accept(chunk);
            }
        }
    }

    public void generate(Heightmap heightmap) {
        Rivermap riverMap = null;
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; dz++) {
                    for (int dx = 0; dx < 16; dx++) {
                        float x = chunk.getBlockX() + dx;
                        float z = chunk.getBlockZ() + dz;
                        Cell cell = chunk.genCell(dx, dz);
                        heightmap.applyBase(cell, x, z);

                        riverMap = Rivermap.get(cell, riverMap, heightmap);
                        heightmap.applyRivers(cell, x, z, riverMap);

                        heightmap.applyClimate(cell, x, z);
                    }
                }
            }
        }
    }

    public void generate(Heightmap heightmap, Batcher batcher) {
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                batcher.submit(new ChunkGenTask(chunk, heightmap));
            }
        }
    }

    public void generate(Heightmap heightmap, float offsetX, float offsetZ, float zoom) {
        Rivermap riverMap = null;
        float translateX = offsetX - ((blockSize.size * zoom) / 2F);
        float translateZ = offsetZ - ((blockSize.size * zoom) / 2F);
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; dz++) {
                    for (int dx = 0; dx < 16; dx++) {
                        float x = ((chunk.getBlockX() + dx) * zoom) + translateX;
                        float z = ((chunk.getBlockZ() + dz) * zoom) + translateZ;
                        Cell cell = chunk.genCell(dx, dz);

                        heightmap.applyBase(cell, x, z);

                        riverMap = Rivermap.get(cell, riverMap, heightmap);
                        heightmap.applyRivers(cell, x, z, riverMap);

                        heightmap.applyClimate(cell, x, z);
                    }
                }
            }
        }
    }

    public void generate(Heightmap heightmap, Batcher batcher, float offsetX, float offsetZ, float zoom) {
        float translateX = offsetX - ((blockSize.size * zoom) / 2F);
        float translateZ = offsetZ - ((blockSize.size * zoom) / 2F);
        batcher.size(chunkSize.total * chunkSize.total);
        for (int cz = 0; cz < chunkSize.total; cz++) {
            for (int cx = 0; cx < chunkSize.total; cx++) {
                int index = chunkSize.indexOf(cx, cz);
                GenChunk chunk = computeChunk(index, cx, cz);
                batcher.submit(new ChunkGenTask.Zoom(chunk, heightmap, translateX, translateZ, zoom));
            }
        }
    }

    public void generateArea(Heightmap heightmap, Batcher batcher, int batchSize) {
        int jobSize = Math.max(1, chunkSize.total / batchSize);
        int jobCount = chunkSize.total / jobSize;
        if (jobCount * jobSize < chunkSize.total) {
            jobCount += 1;
        }

        batcher.size(jobCount * jobCount);

        for (int gz = 0; gz < jobCount; gz++) {
            int cz = gz * jobSize;
            for (int gx = 0; gx < jobCount; gx++) {
                int cx = gx * jobSize;
                batcher.submit(new ChunkBatchTask(cx, cz, jobSize, this, heightmap));
            }
        }
    }

    public void generateArea(Heightmap heightmap, Batcher batcher, int batchSize, float offsetX, float offsetZ, float zoom) {
        int jobSize = Math.max(1, chunkSize.total / batchSize);
        int jobCount = chunkSize.total / jobSize;
        if (jobCount * jobSize < chunkSize.total) {
            jobCount += 1;
        }

        batcher.size(jobCount * jobCount);

        float translateX = offsetX - ((blockSize.size * zoom) / 2F);
        float translateZ = offsetZ - ((blockSize.size * zoom) / 2F);
        for (int gz = 0; gz < jobCount; gz++) {
            int cz = gz * jobSize;
            for (int gx = 0; gx < jobCount; gx++) {
                int cx = gx * jobSize;
                batcher.submit(new ChunkBatchTask.Zoom(cx, cz, jobSize, this, heightmap, translateX, translateZ, zoom));
            }
        }
    }

    public void iterate(Cell.Visitor visitor) {
        for (int dz = 0; dz < blockSize.size; dz++) {
            int z = blockSize.border + dz;
            for (int dx = 0; dx < blockSize.size; dx++) {
                int x = blockSize.border + dx;
                int index = blockSize.indexOf(x, z);
                Cell cell = blocks[index];
                visitor.visit(cell, dx, dz);
            }
        }
    }

    private GenChunk computeChunk(int index, int chunkX, int chunkZ) {
        GenChunk chunk = chunks[index];
        if (chunk == null) {
            chunk = new GenChunk(chunkX, chunkZ);
            chunks[index] = chunk;
        }
        return chunk;
    }

    private Cell computeCell(int index) {
        Cell cell = blocks[index];
        if (cell == null) {
            cell = new Cell();
            blocks[index] = cell;
        }
        return cell;
    }

    public class GenChunk implements ChunkReader, ChunkWriter {

        private final int chunkX;
        private final int chunkZ;
        private final int blockX;
        private final int blockZ;
        private final int regionBlockX;
        private final int regionBlockZ;

        // the coordinate of the chunk within this region (relative to 0,0)
        private GenChunk(int regionChunkX, int regionChunkZ) {
            // the block coordinate of this chunk within this region (relative 0,0)
            this.regionBlockX = regionChunkX << 4;
            this.regionBlockZ = regionChunkZ << 4;
            // the real coordinate of this chunk within the world
            this.chunkX = Tile.this.chunkX + regionChunkX - getOffsetChunks();
            this.chunkZ = Tile.this.chunkZ + regionChunkZ - getOffsetChunks();
            // the real block coordinate of this chunk within the world
            this.blockX = chunkX << 4;
            this.blockZ = chunkZ << 4;
        }

        public GenChunk open() {
            active.getAndIncrement();
            return this;
        }

        @Override
        public void close() {
            active.decrementAndGet();
        }

        @Override
        public void dispose() {
            Tile.this.dispose();
        }

        @Override
        public int getChunkX() {
            return chunkX;
        }

        @Override
        public int getChunkZ() {
            return chunkZ;
        }

        @Override
        public int getBlockX() {
            return blockX;
        }

        @Override
        public int getBlockZ() {
            return blockZ;
        }

        @Override
        public Cell getCell(int blockX, int blockZ) {
            int relX = regionBlockX + (blockX & 15);
            int relZ = regionBlockZ + (blockZ & 15);
            int index = blockSize.indexOf(relX, relZ);
            return blocks[index];
        }

        @Override
        public Cell genCell(int blockX, int blockZ) {
            int relX = regionBlockX + (blockX & 15);
            int relZ = regionBlockZ + (blockZ & 15);
            int index = blockSize.indexOf(relX, relZ);
            return computeCell(index);
        }
    }

    private class FilterRegion implements Filterable {

        @Override
        public Size getSize() {
            return blockSize;
        }

        @Override
        public Cell[] getBacking() {
            return blocks;
        }

        @Override
        public Cell getCellRaw(int x, int z) {
            int index = blockSize.indexOf(x, z);
            if (index < 0 || index >= blockSize.arraySize) {
                return Cell.empty();
            }
            return blocks[index];
        }
    }

    public static long getRegionId(int regionX, int regionZ) {
        return (long) regionX & 4294967295L | ((long) regionZ & 4294967295L) << 32;
    }
}
