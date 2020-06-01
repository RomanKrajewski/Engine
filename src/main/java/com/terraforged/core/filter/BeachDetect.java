package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.heightmap.WorldHeightmap;
import com.terraforged.world.terrain.Terrains;

public class BeachDetect implements Filter, Filter.Visitor {

    private final Terrains terrains;
    private final float grad2;
    private final int radius = 8;
    private final int diameter = radius + 1 + radius;

    public BeachDetect(Terrains terrains) {
        this.terrains = terrains;
        float delta = (8F / 256F) / diameter;
        this.grad2 = delta * delta;
    }

    @Override
    public void apply(Filterable map, int seedX, int seedZ, int iterations) {
        iterate(map, this);
    }

    @Override
    public void visit(Filterable cellMap, Cell cell, int dx, int dz) {
        if (cell.terrain == terrains.coast) {
            if (cell.continentEdge < WorldHeightmap.BEACH_VALUE) {
                Cell n = cellMap.getCellRaw(dx, dz - radius);
                Cell s = cellMap.getCellRaw(dx, dz + radius);
                Cell e = cellMap.getCellRaw(dx + radius, dz);
                Cell w = cellMap.getCellRaw(dx - radius, dz);
                float gx = grad(e, w, cell);
                float gz = grad(n, s, cell);
                float d2 = (gx * gx + gz * gz);
                if (d2 < grad2) {
                    cell.terrain = terrains.beach;
                }
            }
        }
    }

    private float grad(Cell a, Cell b, Cell def) {
        int distance = diameter;
        if (a.isAbsent()) {
            a = def;
            distance -= radius;
        }
        if (b.isAbsent()) {
            b = def;
            distance -= radius;
        }
        return (a.value - b.value) / distance;
    }
}
