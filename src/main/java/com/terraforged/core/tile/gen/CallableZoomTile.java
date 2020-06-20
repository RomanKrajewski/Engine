package com.terraforged.core.tile.gen;

import com.terraforged.core.concurrent.LazyCallable;
import com.terraforged.core.tile.Tile;

public class CallableZoomTile extends LazyCallable<Tile> {

    private final float centerX;
    private final float centerY;
    private final float zoom;
    private final boolean filters;
    private final TileGenerator generator;

    public CallableZoomTile(float centerX, float centerY, float zoom, boolean filters, TileGenerator generator) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.zoom = zoom;
        this.filters = filters;
        this.generator = generator;
    }

    @Override
    protected Tile create() {
        return generator.generateRegion(centerX, centerY, zoom, filters);
    }
}
