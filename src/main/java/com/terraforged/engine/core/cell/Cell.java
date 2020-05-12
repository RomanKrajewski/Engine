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

package com.terraforged.engine.core.cell;

import com.terraforged.engine.core.concurrent.ObjectPool;
import com.terraforged.engine.world.biome.BiomeType;
import com.terraforged.engine.world.terrain.Terrain;
import me.dags.noise.util.NoiseUtil;

public class Cell {

    private static final Cell EMPTY = new Cell() {
        @Override
        public boolean isAbsent() {
            return true;
        }
    };

    private static final ObjectPool<Cell> POOL = new ObjectPool<>(100, Cell::new);

    public int continentX;
    public int continentZ;

    public float continent;
    public float continentEdge;
    public float continentEdgeRaw;

    public float region;
    public float regionEdge;

    public float biome;
    public float biomeEdge = 1F;

    public float riverMask = 1F;
    public boolean riverBed = false;

    public float value;
    public float moisture;
    public float temperature;

    public float steepness;
    public float erosion;
    public float sediment;
    public BiomeType biomeType = BiomeType.GRASSLAND;

    public Terrain tag = null;

    public void copy(Cell other) {
        value = other.value;

        continent = other.continent;
        continentEdge = other.continentEdge;
        continentEdgeRaw = other.continentEdgeRaw;

        region = other.region;
        regionEdge = other.regionEdge;

        biome = other.biome;
        biomeEdge = other.biomeEdge;

        riverMask = other.riverMask;
        riverBed = other.riverBed;

        moisture = other.moisture;
        temperature = other.temperature;

        steepness = other.steepness;
        erosion = other.erosion;
        sediment = other.sediment;
        biomeType = other.biomeType;

        tag = other.tag;
    }

    public float continentMask(float min, float max) {
        return NoiseUtil.map(continentEdge, min, max, max - min);
    }

    public float regionMask(float min, float max) {
        return NoiseUtil.map(regionEdge, min, max, max - min);
    }

    public float biomeMask(float min, float max) {
        return NoiseUtil.map(biomeEdge, min, max, max - min);
    }

    public float mask(float cmin, float cmax, float rmin, float rmax) {
        return riverMask * continentMask(cmin, cmax) * regionMask(rmin, rmax);
    }

    public boolean isAbsent() {
        return false;
    }

    public static Cell empty() {
        return EMPTY;
    }

    public static ObjectPool.Item<Cell> pooled() {
        return POOL.get();
    }

    public interface Visitor {

        void visit(Cell cell, int dx, int dz);
    }

    public interface ContextVisitor<C> {

        void visit(Cell cell, int dx, int dz, C ctx);
    }
}
