package com.terraforged.core.region.gen;

import com.terraforged.core.region.Region;
import com.terraforged.core.concurrent.Disposable;
import com.terraforged.core.concurrent.batcher.Batcher;

public class RegionGeneratorBatched extends RegionGenerator {

    public RegionGeneratorBatched(Builder builder) {
        super(builder);
    }

    private RegionGeneratorBatched(RegionGenerator from, Disposable.Listener<Region> listener) {
        super(from, listener);
    }

    @Override
    public RegionGenerator withListener(Disposable.Listener<Region> listener) {
        return new RegionGeneratorBatched(this, listener);
    }

    @Override
    public Region generateRegion(int regionX, int regionZ) {
        Region region = new Region(regionX, regionZ, factor, border, disposalListener);
        try (Batcher batcher = threadPool.batcher()) {
            region.generateArea(generator.getHeightmap(), batcher, batchSize);
        }
        postProcess(region);
        return region;
    }

    @Override
    public Region generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        Region region = new Region(0, 0, factor, border, disposalListener);
        try (Batcher batcher = threadPool.batcher()) {
            region.generateArea(generator.getHeightmap(), batcher, batchSize, centerX, centerZ, zoom);
        }
        postProcess(region, centerX, centerZ, zoom, filter);
        return region;
    }
}
