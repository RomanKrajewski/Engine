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
