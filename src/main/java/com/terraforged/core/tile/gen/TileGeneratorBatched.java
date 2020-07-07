package com.terraforged.core.tile.gen;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;
import com.terraforged.core.tile.Tile;

public class TileGeneratorBatched extends TileGenerator {

    public TileGeneratorBatched(Builder builder) {
        super(builder);
    }

    @Override
    public Tile generateRegion(int regionX, int regionZ) {
        Tile tile = createEmptyRegion(regionX, regionZ);
        try (Resource<Batcher> batcher = threadPool.batcher()) {
            tile.generateArea(generator.getHeightmap(), batcher.get(), batchSize);
        }
        postProcess(tile);
        return tile;
    }

    @Override
    public Tile generateRegion(float centerX, float centerZ, float zoom, boolean filter) {
        Tile tile = createEmptyRegion(0, 0);
        try (Resource<Batcher> batcher = threadPool.batcher()) {
            tile.generateArea(generator.getHeightmap(), batcher.get(), batchSize, centerX, centerZ, zoom);
        }
        postProcess(tile, filter);
        return tile;
    }
}
