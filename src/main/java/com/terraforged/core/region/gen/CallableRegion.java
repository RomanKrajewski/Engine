package com.terraforged.core.region.gen;

import com.terraforged.core.region.Region;

public class CallableRegion extends GenCallable<Region> {

    private final int regionX;
    private final int regionZ;
    private final RegionGenerator generator;

    public CallableRegion(int regionX, int regionZ, RegionGenerator generator) {
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.generator = generator;
    }

    @Override
    protected Region create() {
        return generator.generateRegion(regionX, regionZ);
    }
}
