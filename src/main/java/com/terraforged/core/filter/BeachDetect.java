package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.heightmap.TransitionPoints;
import com.terraforged.world.terrain.Terrains;

public class BeachDetect implements Filter, Filter.Visitor {

    private final Terrains terrains;
    private final TransitionPoints transition;
    private final float grad2;
    private final int radius = 8;
    private final int diameter = radius + 1 + radius;

    public BeachDetect(GeneratorContext context) {
        this.terrains = context.terrain;
        this.transition = new TransitionPoints(context.settings.world.transitionPoints);
        float delta = (8F / 256F) / diameter;
        this.grad2 = delta * delta;
    }

    @Override
    public void apply(Filterable map, int seedX, int seedZ, int iterations) {
        iterate(map, this);
    }

    @Override
    public void visit(Filterable cellMap, Cell cell, int dx, int dz) {
        if (cell.terrain.isCoast() && cell.continentEdge < transition.coast) {
            Cell n = cellMap.getCellRaw(dx, dz - radius);
            Cell s = cellMap.getCellRaw(dx, dz + radius);
            Cell e = cellMap.getCellRaw(dx + radius, dz);
            Cell w = cellMap.getCellRaw(dx - radius, dz);
            float gx = grad(e, w, cell);
            float gz = grad(n, s, cell);
            float d2 = (gx * gx + gz * gz);
            if (d2 < 0.275F) {
                cell.terrain = terrains.beach;
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
