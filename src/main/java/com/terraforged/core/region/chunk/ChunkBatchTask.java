package com.terraforged.core.region.chunk;

import com.terraforged.core.concurrent.batcher.BatchNotifier;
import com.terraforged.core.concurrent.batcher.BatchedTask;
import com.terraforged.core.region.Region;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.rivermap.Rivermap;

public class ChunkBatchTask implements BatchedTask {

    private final int x;
    private final int z;
    private final int size;
    private final Region region;
    private final Heightmap heightmap;

    private BatchNotifier notifier = BatchNotifier.NONE;
    protected Rivermap rivers = null;

    public ChunkBatchTask(int x, int z, int size, Region region, Heightmap heightmap) {
        this.heightmap = heightmap;
        this.region = region;
        this.x = x;
        this.z = z;
        this.size = size;
    }

    @Override
    public void setBatcher(BatchNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void run() {
        try {
            drive();
        } finally {
            notifier.markDone();
        }
    }

    private void drive() {
        for (int dz = 0; dz < size; dz++) {
            int cz = z + dz;
            if (cz > region.getChunkSize().total) {
                continue;
            }
            for (int dx = 0; dx < size; dx++) {
                int cx = x + dx;
                if (cx > region.getChunkSize().total) {
                    continue;
                }
                try {
                    driveOne(region.genChunk(cx, cz), heightmap);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    protected void driveOne(ChunkWriter chunk, Heightmap heightmap) {
        chunk.generate((cell, dx, dz) -> {
            float x = chunk.getBlockX() + dx;
            float z = chunk.getBlockZ() + dz;
            heightmap.apply(cell, x, z);
            rivers = Rivermap.get(cell, rivers, heightmap);
            rivers.apply(cell, x, z);
            heightmap.applyClimate(cell, x, z);
        });
    }

    public static class Zoom extends ChunkBatchTask {

        private final float translateX;
        private final float translateZ;
        private final float zoom;

        public Zoom(int x, int z, int size, Region region, Heightmap heightmap, float translateX, float translateZ, float zoom) {
            super(x, z, size, region, heightmap);
            this.translateX = translateX;
            this.translateZ = translateZ;
            this.zoom = zoom;
        }

        @Override
        protected void driveOne(ChunkWriter chunk, Heightmap heightmap) {
            chunk.generate((cell, dx, dz) -> {
                float x = ((chunk.getBlockX() + dx) * zoom) + translateX;
                float z = ((chunk.getBlockZ() + dz) * zoom) + translateZ;
                heightmap.apply(cell, x, z);
                rivers = Rivermap.get(cell, rivers, heightmap);
                rivers.apply(cell, x, z);
                heightmap.applyClimate(cell, x, z);
            });
        }
    }
}
