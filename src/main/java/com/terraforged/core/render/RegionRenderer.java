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

import com.terraforged.core.cell.Cell;
import com.terraforged.core.tile.Tile;

public class RegionRenderer {

    public static final float RENDER_SCALE = 1F;

    private final RenderSettings settings;
    private final RenderAPI context;

    public RegionRenderer(RenderAPI context, RenderSettings settings) {
        this.context = context;
        this.settings = settings;
    }

    public RenderSettings getSettings() {
        return settings;
    }

    public RenderRegion render(Tile tile) {
        RenderRegion renderRegion = new RenderRegion(tile);
        render(renderRegion);
        return renderRegion;
    }

    public void render(RenderRegion region) {
        region.clear();
        int resolution = settings.resolution;
        float w = settings.width / (resolution - 1F);
        float h = settings.width / (resolution - 1F);
        float unit = w / settings.zoom;
        RenderBuffer shape = context.createBuffer();
        shape.beginQuads();
        shape.noFill();
        for (int dy = 0; dy < resolution; dy++) {
            for (int dx = 0; dx < resolution; dx++) {
                draw(shape, region.getTile(), dx, dy, resolution, w, h, unit);
            }
        }
        shape.endQuads();
        region.setMesh(shape);
    }

    private void draw(RenderBuffer shape, Tile tile, int dx, int dz, int resolution, float w, float h, float unit) {
        Cell cell = tile.getCell(dx, dz);
        if (cell == null) {
            return;
        }

        float height = cell.value * settings.levels.worldHeight;
        float x = dx * w;
        float z = dz * h;
        int y = getY(height, unit);
        settings.renderMode.fillColor(cell, height, shape, settings);

        shape.vertex(x, z, y);
        shape.vertex(x + w, z, y);
        shape.vertex(x + w, z + w, y);
        shape.vertex(x, z + w, y);

        if (dx <= 0 && dz <= 0) {
            drawEdge(shape, dx, y, dz, w, h, true);
            drawEdge(shape, dx, y, dz, w, h, false);
            return;
        }

        if (dx >= resolution - 1 && dz >= resolution - 1) {
            drawEdge(shape, dx + 1, y, dz, w, h, true);
            drawEdge(shape, dx, y, dz + 1, w, h, false);
            return;
        }

        if (dx <= 0 && dz >= resolution - 1) {
            drawEdge(shape, dx, y, dz, w, h, true);
            drawEdge(shape, dx, y, dz + 1, w, h, false);
            return;
        }

        if (dz <= 0 && dx >= resolution - 1) {
            drawEdge(shape, dx, y, dz, w, h, false);
            drawEdge(shape, dx + 1, y, dz, w, h, true);
            drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
            return;
        }

        if (dx <= 0) {
            drawEdge(shape, dx, y, dz, w, h, true);
            drawFace(shape, tile, dx, y, dz, dx, dz - 1, w, h, unit);
            return;
        }
        if (dz <= 0) {
            drawEdge(shape, dx, y, dz, w, h, false);
            drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
            return;
        }

        if (dx >= resolution - 1) {
            drawEdge(shape, dx + 1, y, dz, w, h, true);
            drawFace(shape, tile, dx, y, dz, dx, dz - 1, w, h, unit);
            drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
            return;
        }

        if (dz >= resolution - 1) {
            drawEdge(shape, dx, y, dz + 1, w, h, false);
            drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
            drawFace(shape, tile, dx, y, dz, dx, dz - 1, w, h, unit);
            return;
        }

        drawFace(shape, tile, dx, y, dz, dx - 1, dz, w, h, unit);
        drawFace(shape, tile, dx, y, dz, dx, dz - 1, w, h, unit);
    }

    private void drawFace(RenderBuffer shape, Tile tile, int px, int py, int pz, int dx, int dz, float w, float h, float unit) {
        Cell cell = tile.getCell(dx, dz);
        if (cell == null) {
            return;
        }

        float height = cell.value * settings.levels.worldHeight;
        int y = getY(height, unit);
        if (y == py) {
            return;
        }

        if (dx != px) {
            shape.vertex(px * w, pz * h, py);
            shape.vertex(px * w, (pz + 1) * h, py);
            shape.vertex(px * w, (pz + 1) * h, y);
            shape.vertex(px * w, pz * h, y);
        } else {
            shape.vertex(px * w, pz * h, py);
            shape.vertex((px + 1) * w, pz * h, py);
            shape.vertex((px + 1) * w, pz * h, y);
            shape.vertex(px * w, pz * h, y);
        }
    }

    private void drawEdge(RenderBuffer shape, int px, int py, int pz, float w, float h, boolean x) {
        int y = 0;

        if (x) {
            shape.vertex(px * w, pz * h, py);
            shape.vertex(px * w, (pz + 1) * h, py);
            shape.vertex(px * w, (pz + 1) * h, y);
            shape.vertex(px * w, pz * h, y);
        } else {
            shape.vertex(px * w, pz * h, py);
            shape.vertex((px + 1) * w, pz * h, py);
            shape.vertex((px + 1) * w, pz * h, y);
            shape.vertex(px * w, pz * h, y);
        }
    }

    private int getY(float height, float unit) {
        if (height <= settings.levels.waterLevel) {
            return (int) (settings.levels.waterLevel * unit);
        }
        return (int) ((int) height * unit);
    }
}
