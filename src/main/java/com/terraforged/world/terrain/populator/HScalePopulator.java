package com.terraforged.world.terrain.populator;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Module;
import com.terraforged.world.terrain.Terrain;

public class HScalePopulator extends TerrainPopulator {

    private final float frequency;

    public HScalePopulator(Terrain type, Module base, Module variance, float horizontalScale) {
        super(type, base, variance);
        frequency = 1F / horizontalScale;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        super.apply(cell, x * frequency, z * frequency);
    }
}
