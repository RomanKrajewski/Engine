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

package com.terraforged.engine.core.region.chunk;

import com.terraforged.engine.world.heightmap.Heightmap;
import com.terraforged.engine.world.rivermap.Rivermap;

public class ChunkGenTask implements Runnable {

    private final ChunkWriter chunk;
    private final Heightmap heightmap;
    protected Rivermap rivers = null;

    public ChunkGenTask(ChunkWriter chunk, Heightmap heightmap) {
        this.chunk = chunk;
        this.heightmap = heightmap;
    }

    @Override
    public void run() {
        driveOne(chunk, heightmap);
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

    public static class Zoom extends ChunkGenTask {

        private final float translateX;
        private final float translateZ;
        private final float zoom;

        public Zoom(ChunkWriter chunk, Heightmap heightmap, float translateX, float translateZ, float zoom) {
            super(chunk, heightmap);
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
