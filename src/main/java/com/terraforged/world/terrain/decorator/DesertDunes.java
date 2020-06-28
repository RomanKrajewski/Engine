/*
 *   
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

package com.terraforged.world.terrain.decorator;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.func.CellFunc;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;

public class DesertDunes implements Decorator {

    private final Module module;
    private final Levels levels;
    private final Terrains terrains;

    public DesertDunes(GeneratorContext context) {
        this.levels = context.levels;
        this.terrains = context.terrain;
        this.module = Source.cell(context.seed.next(), 80, CellFunc.DISTANCE)
                .warp(context.seed.next(), 70, 1, 70)
                .scale(context.levels.scale(35));
    }

    @Override
    public boolean apply(Cell cell, float x, float y) {
        if (BiomeType.DESERT != cell.biomeType) {
            return false;
        }

        float max = levels.water(40);
        float min = levels.water(30);
        if (cell.value > max) {
            return false;
        }

        float fade = 1;
        if (cell.value > min) {
            fade = 1 - (cell.value - min) / (max - min);
        }

        float duneHeight = module.getValue(x, y);
        float mask = cell.biomeEdge * cell.riverMask * cell.regionEdge * fade;

        float height = duneHeight * mask;
        cell.value += height;
        cell.terrain = terrains.desert;

        return height >= levels.unit;
    }
}
