package com.terraforged.world.terrain.populator;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Module;
import com.terraforged.world.terrain.Terrain;

public class HVScalePopulator extends VScalePopulator {

    private final float frequency;

    public HVScalePopulator(Terrain type, Module base, Module variance, float baseScale, float varianceScale, float horizontalScale) {
        super(type, base, variance, baseScale, varianceScale);
        this.frequency = 1F / horizontalScale;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        super.apply(cell, x * frequency, z * frequency);
    }
}
