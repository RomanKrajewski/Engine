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

package com.terraforged.world.terrain;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import me.dags.noise.Module;

public class TerrainPopulator implements Populator {

    private final Terrain type;
    private final Module source;

    public TerrainPopulator(Module source, Terrain type) {
        this.type = type;
        this.source = clamp(source);
    }

    public Module getSource() {
        return source;
    }

    public Terrain getType() {
        return type;
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        cell.value = source.getValue(x, z);
        cell.terrain = type;
    }

    @Override
    public void tag(Cell cell, float x, float y) {
        cell.terrain = type;
    }

    public static Module clamp(Module module) {
        if (module.minValue() < 0 || module.maxValue() > 1) {
            return module.clamp(0, 1);
        }
        return module;
    }
}
