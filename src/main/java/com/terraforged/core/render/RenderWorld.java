package com.terraforged.core.render;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.cache.CacheEntry;
import com.terraforged.core.concurrent.thread.ThreadPool;
import com.terraforged.core.concurrent.thread.ThreadPools;
import com.terraforged.core.tile.Size;
import com.terraforged.core.tile.Tile;
import com.terraforged.core.tile.gen.TileGenerator;

public class RenderWorld {

    private final int regionCount;
    private final Size regionSize;
    private final RenderAPI context;
    private final RegionRenderer renderer;
    private final TileGenerator generator;
    private final RenderRegion[] view;
    private final CacheEntry<RenderRegion>[] queue;
    private final ThreadPool threadPool = ThreadPools.getUtilPool();

    public RenderWorld(TileGenerator generator, RenderAPI context, RenderSettings settings, int regionCount, int regionSize) {
        this.context = context;
        this.generator = generator;
        this.regionCount = regionCount;
        this.renderer = new RegionRenderer(context, settings);
        this.regionSize = Size.blocks(regionSize, 0);
        this.queue = new CacheEntry[regionCount * regionCount];
        this.view = new RenderRegion[regionCount * regionCount];
    }

    public int getResolution() {
        return regionSize.total;
    }

    public int getSize() {
        return regionSize.total * regionCount;
    }

    public boolean isRendering() {
        for (CacheEntry<?> entry : queue) {
            if (entry != null) {
                return true;
            }
        }
        return false;
    }

    public Cell getCenter() {
        float cx = regionCount / 2F;
        float cz = regionCount / 2F;

        int rx = (int) cx;
        int rz = (int) cz;
        int index = rx + regionCount * rz;
        RenderRegion renderRegion = view[index];
        if (renderRegion == null) {
            return Cell.empty();
        }

        float ox = cx - rx;
        float oz = cz - rz;
        Tile tile = renderRegion.getTile();
        int dx = (int) (tile.getBlockSize().size * ox);
        int dz = (int) (tile.getBlockSize().size * oz);
        return tile.getCell(dx, dz);
    }

    public void redraw() {
        for (RenderRegion region : view) {
            if (region != null) {
                renderer.render(region);
            }
        }
    }

    public void refresh() {
        for (CacheEntry<?> entry : queue) {
            if (entry != null && !entry.isDone()) {
                return;
            }
        }
        for (int i = 0; i < queue.length; i++) {
            CacheEntry<RenderRegion> entry = queue[i];
            if (entry == null) {
                continue;
            }
            if (entry.isDone()) {
                queue[i] = null;
                view[i] = entry.get();
            }
        }
    }

    public void update(float x, float y, float zoom, boolean filters) {
        renderer.getSettings().zoom = zoom;
        renderer.getSettings().resolution = getResolution();
        float factor = regionCount > 1 ? (regionCount - 1F) / regionCount : 0F;
        float offset = regionSize.size * zoom * factor;
        for (int rz = 0; rz < regionCount; rz++) {
            for (int rx = 0; rx < regionCount; rx++) {
                int index = rx + rz * regionCount;
                float px = x + (rx * regionSize.size * zoom) - offset;
                float py = y + (rz * regionSize.size * zoom) - offset;
                queue[index] = generator.getAsync(px, py, zoom, filters).then(threadPool, renderer::render);
            }
        }
    }

    public void render() {
        int resolution = getResolution();
        float w = renderer.getSettings().width / (float) (resolution - 1);
        float h = renderer.getSettings().width / (float) (resolution - 1);
        float offsetX = (regionSize.size * regionCount * w) / 2F;
        float offsetY = (regionSize.size * regionCount * w) / 2F;

        context.pushMatrix();
        context.translate(-offsetX, -offsetY, 0F);
        for (int rz = 0; rz < regionCount; rz++) {
            for (int rx = 0; rx < regionCount; rx++) {
                int index = rx + rz * regionCount;
                RenderRegion region = view[index];
                if (region == null) {
                    continue;
                }

                context.pushMatrix();
                float x = rx * regionSize.size * w;
                float z = rz * regionSize.size * h;
                context.translate(x, z, 0);
                region.getMesh().draw();
                context.popMatrix();
            }
        }
        context.popMatrix();
    }
}
