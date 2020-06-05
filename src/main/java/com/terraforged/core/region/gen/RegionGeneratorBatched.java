package com.terraforged.core.region.gen;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;
import com.terraforged.core.region.Region;

public class RegionGeneratorBatched extends RegionGenerator {

    public RegionGeneratorBatched(Builder builder) {
        super(builder);
    }

    @Override
    public Region generateRegion(int regionX, int regionZ) {
        Region region = createRegion(regionX, regionZ, factor, border);
        try (Resource<Batcher> batcher = threadPool.batcher()) {
            region.generateArea(generator.getHeightmap(), batcher.get(), batchSize);
        }
        postProcess(region);
        return region;
    }

    @Override
    public Region generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        Region region = createRegion(0, 0, factor, border);
        try (Resource<Batcher> batcher = threadPool.batcher()) {
            region.generateArea(generator.getHeightmap(), batcher.get(), batchSize, centerX, centerZ, zoom);
        }
        postProcess(region, centerX, centerZ, zoom, filter);
        return region;
    }
}
