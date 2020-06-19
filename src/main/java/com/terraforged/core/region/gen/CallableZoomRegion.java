package com.terraforged.core.region.gen;

import com.terraforged.core.region.Region;

public class CallableZoomRegion extends GenCallable<Region> {

    private final float centerX;
    private final float centerY;
    private final float zoom;
    private final boolean filters;
    private final RegionGenerator generator;

    public CallableZoomRegion(float centerX, float centerY, float zoom, boolean filters, RegionGenerator generator) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
        this.filters = filters;
        this.generator = generator;
    }

    @Override
    protected Region create() {
        return generator.generateRegion(centerX, centerY, zoom, filters);
    }
}
