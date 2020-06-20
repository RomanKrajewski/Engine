package com.terraforged.world.terrain.populator;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Module;
import com.terraforged.world.terrain.Terrain;

public class VScalePopulator extends TerrainPopulator {

    private final float baseScale;
    private final float varianceScale;

    public VScalePopulator(Terrain type, Module base, Module variance, float baseScale, float varianceScale) {
        super(type, base, variance);
        this.baseScale = baseScale;
        this.varianceScale = varianceScale;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        float base = this.base.getValue(x, z) * baseScale;
        float variance = this.variance.getValue(x, z) * varianceScale;

        cell.value = base + variance;

        if (cell.value < 0) {
            cell.value = 0;
        } else if (cell.value > 1) {
            cell.value = 1;
        }

        cell.terrain = type;
    }
}
