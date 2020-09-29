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

import com.terraforged.core.tile.Tile;

public class RenderRegion {

    private final Tile tile;
    private final Object lock = new Object();
    private RenderBuffer mesh;

    public RenderRegion(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    public RenderBuffer getMesh() {
        synchronized (lock) {
            return mesh;
        }
    }

    public void setMesh(RenderBuffer mesh) {
        synchronized (lock) {
            this.mesh = mesh;
        }
    }

    public void clear() {
        synchronized (lock) {
            if (mesh != null) {
                mesh.dispose();
                mesh = null;
            }
        }
    }
}
