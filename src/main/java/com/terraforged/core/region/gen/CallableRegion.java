package com.terraforged.core.region.gen;

import com.terraforged.core.region.Region;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CallableRegion implements Callable<Region>, Future<Region> {

    private final int regionX;
    private final int regionZ;
    private final RegionGenerator generator;

    private volatile Region region;

    public CallableRegion(int regionX, int regionZ, RegionGenerator generator) {
        super();
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.generator = generator;
    }

    @Override
    public Region call() {
        if (region == null) {
            Region result = generator.generateRegion(regionX, regionZ);
            region = result;
            return result;
        }
        return region;
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
