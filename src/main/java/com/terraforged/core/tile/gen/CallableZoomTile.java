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

package com.terraforged.core.tile.gen;

import com.terraforged.core.concurrent.LazyCallable;
import com.terraforged.core.tile.Tile;

public class CallableZoomTile extends LazyCallable<Tile> {

    private final float centerX;
    private final float centerY;
    private final float zoom;
    private final boolean filters;
    private final TileGenerator generator;

    public CallableZoomTile(float centerX, float centerY, float zoom, boolean filters, TileGenerator generator) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
        this.filters = filters;
        this.generator = generator;
    }

    @Override
    protected Tile create() {
        return generator.generateRegion(centerX, centerY, zoom, filters);
    }
}
