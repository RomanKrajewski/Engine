package com.terraforged.core.tile.gen;

import com.terraforged.core.concurrent.LazyCallable;
import com.terraforged.core.tile.Tile;

public class CallableTile extends LazyCallable<Tile> {

    private final int regionX;
    private final int regionZ;
    private final TileGenerator generator;

    public CallableTile(int regionX, int regionZ, TileGenerator generator) {
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.generator = generator;
    }

    @Override
    protected Tile create() {
        return generator.generateRegion(regionX, regionZ);
    }
}
