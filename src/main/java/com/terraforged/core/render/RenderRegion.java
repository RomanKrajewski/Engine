package com.terraforged.core.render;

import com.terraforged.core.region.Region;

public class RenderRegion {

    private final Region region;
    private final Object lock = new Object();
    private RenderBuffer mesh;

    public RenderRegion(Region region) {
        this.region = region;
    }

    public Region getRegion() {
        return region;
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
