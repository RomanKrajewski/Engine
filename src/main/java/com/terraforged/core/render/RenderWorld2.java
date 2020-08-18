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

package com.terraforged.core.render;

import com.terraforged.core.concurrent.cache.CacheEntry;
import com.terraforged.core.concurrent.thread.ThreadPools;
import com.terraforged.core.tile.Size;
import com.terraforged.core.tile.gen.TileCache;
import com.terraforged.core.tile.gen.TileGenerator;
import com.terraforged.core.util.PosIterator;
import com.terraforged.core.util.RollingGrid;

public class RenderWorld2 implements RollingGrid.Generator<RenderWorld2.RegionHolder> {

    private final int factor;
    private final Size regionSize;
    private final TileCache cache;
    private final TileGenerator generator;
    private final RenderAPI context;
    private final RegionRenderer renderer;
    private final RollingGrid<RegionHolder> world;

    private boolean first = true;

    public RenderWorld2(TileGenerator generator, RenderAPI context, RenderSettings settings, int regionCount, int regionSize) {
        this.context = context;
        this.factor = regionSize;
        this.generator = generator;
        this.cache = generator.toCache();
        this.regionSize = Size.blocks(regionSize, 0);
        this.renderer = new RegionRenderer(context, settings);
        this.world = new RollingGrid<>(regionCount, RegionHolder[]::new, this);
    }

    public boolean isBusy() {
        for (RegionHolder h : world.getIterator()) {
            if (h != null && !h.region.isDone()) {
                return true;
            }
        }
        return false;
    }

    public int getResolution() {
        return regionSize.total * world.getSize();
    }

    public int blockToRegion(int value) {
        return value >> factor;
    }

    public void init(int centerX, int centerZ) {
        renderer.getSettings().zoom = 1F;
        renderer.getSettings().resolution = regionSize.total;
        PosIterator iterator = PosIterator.area(0, 0, world.getSize(), world.getSize());
        while (iterator.next()) {
            RegionHolder holder = generate(iterator.x(), iterator.z());
            world.set(iterator.x(), iterator.z(), holder);
        }
    }

    public void move(int centerX, int centerZ) {
        if (first) {
            first = false;
            init(centerX, centerZ);
        } else {
            renderer.getSettings().zoom = 1F;
            renderer.getSettings().resolution = regionSize.total;
            world.move(centerX, centerZ);
        }
    }

    public void render() {
        int resolution = regionSize.total;
        float w = (renderer.getSettings().width * RegionRenderer.RENDER_SCALE) / (float) (resolution - 1);
        float h = (renderer.getSettings().width * RegionRenderer.RENDER_SCALE) / (float) (resolution - 1);

        float offsetX = (world.getSize() * regionSize.size * w) / 2F;
        float offsetZ = (world.getSize() * regionSize.size * h) / 2F;

        context.pushMatrix();
        context.translate(-offsetX, -offsetZ, 1000);

        PosIterator iterator = PosIterator.area(0, 0, world.getSize(), world.getSize());
        while (iterator.next()) {
            RegionHolder holder = world.get(iterator.x(), iterator.z());
            if (holder == null || !holder.region.isDone()) {
                continue;
            }

            int relX = iterator.x();
            int relZ = iterator.z();
            float startX = relX * regionSize.size * w;
            float startZ = relZ * regionSize.size * h;

            RenderRegion region = holder.region.get();
            context.pushMatrix();
            context.translate(startX, startZ, 0);
            region.getMesh().draw();
            context.popMatrix();
        }

        context.popMatrix();
    }

    @Override
    public RegionHolder generate(int x, int z) {
        return new RegionHolder(generator.getAsync(x, z).then(ThreadPools.getUtilPool(), renderer::render));
    }

    public static class RegionHolder {

        private final CacheEntry<RenderRegion> region;

        private RegionHolder(CacheEntry<RenderRegion> region) {
            this.region = region;
        }
    }
}
