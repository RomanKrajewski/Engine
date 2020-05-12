package com.terraforged.engine.core.region.gen;

import com.terraforged.engine.core.region.Region;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CallableZoomRegion implements Callable<Region>, Future<Region> {

    private final float centerX;
    private final float centerY;
    private final float zoom;
    private final boolean filters;
    private final RegionGenerator generator;

    private volatile Region region;

    public CallableZoomRegion(float centerX, float centerY, float zoom, boolean filters, RegionGenerator generator) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
        this.filters = filters;
        this.generator = generator;
    }

    @Override
    public Region call() {
        Region result = region;
        if (result == null) {
            result = generator.generateRegion(centerX, centerY, zoom, filters);
            region = result;
        }
        return result;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return region != null;
    }

    @Override
    public Region get(){
        return call();
    }

    @Override
    public Region get(long timeout, TimeUnit unit) {
        return get();
    }
}
