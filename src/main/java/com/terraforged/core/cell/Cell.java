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

package com.terraforged.core.cell;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.pool.ObjectPool;
import com.terraforged.core.concurrent.thread.context.ContextualThread;
import com.terraforged.world.biome.BiomeType;
import com.terraforged.world.terrain.Terrain;

// General Notes:
// - all float are values expected to be within the range 0.0 - 1.0 inclusive
// - an 'identity' is a value that is constant for all cells within a voronoi cell
// - the 'edge' value is the corresponding worley/distance noise for that voronoi cell
public class Cell {

    // purely used to reset a cell's to defaults
    // this is separate to the public Cell.EMPTY as that may accidentally be mutated
    private static final Cell defaults = new Cell();

    // represents a 'null' cell
    private static final Cell EMPTY = new Cell() {
        @Override
        public boolean isAbsent() {
            return true;
        }
    };

    private static final ObjectPool<Cell> POOL = new ObjectPool<>(32, Cell::new);

    public int continentX;
    public int continentZ;
    public float continentEdge;
    public float continentIdentity;

    public float terrainRegionEdge;
    public float terrainRegionIdentity;
    public Terrain terrain = Terrain.NONE;

    public float biomeEdge = 1F;
    public float biomeIdentity;

    // represents the distance to a river/lake. value decreases the closer to a river the cell is
    public float riverMask = 1F;
    // a flag that tells the erosion filter not to apply any changes to this cell
    public boolean erosionMask = false;

    // the actual height data
    public float value;

    public float waterLevel = 0F;

    // climate data
    public float moisture = 0.5F;
    public float temperature = 0.5F;
    public BiomeType biomeType = BiomeType.GRASSLAND;

    // random noise assigned to a large biome-aligned area
    // current use-case is to change all sand biomes within a certain area to a single colour (yellow or red)
    public float macroNoise;

    // how steep the surface is at this cell's location
    public float gradient;
    // how much material was eroded at this cell's location
    public float erosion;
    // how much material was deposited at this cell's location
    public float sediment;

    public void copy(Cell other) {
        value = other.value;

        waterLevel = other.waterLevel;

        continentIdentity = other.continentIdentity;
        continentEdge = other.continentEdge;

        terrainRegionIdentity = other.terrainRegionIdentity;
        terrainRegionEdge = other.terrainRegionEdge;

        biomeIdentity = other.biomeIdentity;
        biomeEdge = other.biomeEdge;

        riverMask = other.riverMask;
        erosionMask = other.erosionMask;

        moisture = other.moisture;
        temperature = other.temperature;
        macroNoise = other.macroNoise;

        gradient = other.gradient;
        erosion = other.erosion;
        sediment = other.sediment;
        biomeType = other.biomeType;

        terrain = other.terrain;
    }

    public void reset() {
        copy(defaults);
    }

    public boolean isAbsent() {
        return false;
    }

    public static Cell empty() {
        return EMPTY;
    }

    public static Resource<Cell> pooled() {
        // prefer obtaining a cell from ContextualThreads
        Thread current = Thread.currentThread();
        if (current instanceof ContextualThread) {
            ContextualThread contextual = (ContextualThread) current;
            return contextual.getContext().cell;
        } else {
            Resource<Cell> item = POOL.get();
            item.get().reset();
            return item;
        }
    }

    public interface Visitor {

        void visit(Cell cell, int dx, int dz);
    }

    public interface ContextVisitor<C> {

        void visit(Cell cell, int dx, int dz, C ctx);
    }
}
