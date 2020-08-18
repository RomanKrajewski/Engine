/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.heightmap.ControlPoints;
import com.terraforged.world.terrain.Terrains;

public class BeachDetect implements Filter, Filter.Visitor {

    private final Terrains terrains;
    private final ControlPoints transition;
    private final float grad2;
    private final int radius = 8;
    private final int diameter = radius + 1 + radius;

    public BeachDetect(GeneratorContext context) {
        this.terrains = context.terrain;
        this.transition = new ControlPoints(context.settings.world.controlPoints);
        float delta = (8F / 256F) / diameter;
        this.grad2 = delta * delta;
    }

    @Override
    public void apply(Filterable map, int seedX, int seedZ, int iterations) {
        iterate(map, this);
    }

    @Override
    public void visit(Filterable cellMap, Cell cell, int dx, int dz) {
        if (cell.terrain.isCoast() && cell.continentEdge < transition.beach) {
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
