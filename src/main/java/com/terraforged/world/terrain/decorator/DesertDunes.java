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
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrain;
import com.terraforged.world.terrain.Terrains;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.CellFunc;

public class DesertDunes implements Decorator {

    private final Module module;
    private final float climateMin;
    private final float climateMax;
    private final float climateRange;

    private final Levels levels;
    private final Terrains terrains;
    private final Terrain dunes = new Terrain("dunes", 1) {
        @Override
        public boolean isSandy() {
            return true;
        }
    };

    public DesertDunes(GeneratorContext context) {
        this.climateMin = 0.6F;
        this.climateMax = 0.85F;
        this.climateRange = climateMax - climateMin;
        this.levels = context.levels;
        this.terrains = context.terrain;
        this.module = Source.cell(context.seed.next(), 80, CellFunc.DISTANCE)
                .warp(context.seed.next(), 70, 1, 70)
                .scale(30 / 255D);
    }

    @Override
    public boolean apply(Cell cell, float x, float y) {
        if (BiomeType.DESERT != cell.biomeType) {
            return false;
        }

        if (cell.terrainType != terrains.plains && cell.terrainType != terrains.steppe) {
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
        cell.terrainType = dunes;

        return height >= levels.unit;
    }
}
