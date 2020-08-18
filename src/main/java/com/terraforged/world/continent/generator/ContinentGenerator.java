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

package com.terraforged.world.continent.generator;

import com.terraforged.core.NumConstants;
import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.WorldSettings;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.domain.Domain;
import com.terraforged.n2d.func.DistanceFunc;
import com.terraforged.n2d.func.EdgeFunc;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.n2d.util.Vec2f;
import com.terraforged.world.continent.Continent;
import com.terraforged.world.continent.MutableVeci;
import com.terraforged.world.heightmap.ControlPoints;

public abstract class ContinentGenerator implements Continent {

    protected final int seed;
    protected final float frequency;
    protected final int continentScale;

    private final DistanceFunc distanceFunc;
    private final ControlPoints controlPoints;

    private final float clampMin;
    private final float clampMax;
    private final float clampRange;

    protected final Domain warp;
    protected final Module shape;

    public ContinentGenerator(Seed seed, WorldSettings settings) {
        int tectonicScale = settings.continent.continentScale * 4;

        this.continentScale = settings.continent.continentScale / 2;
        this.seed = seed.next();
        this.distanceFunc = settings.continent.continentShape;
        this.controlPoints = new ControlPoints(settings.controlPoints);
        this.frequency = 1F / tectonicScale;

        this.clampMin = 0.2F;
        this.clampMax = 1.0F;
        this.clampRange = clampMax - clampMin;

//        this.warp = Domain.warp(Source.PERLIN, seed.next(), 20, 2, 20)
//                .warp(Domain.warp(Source.SIMPLEX, seed.next(), continentScale, 3, continentScale));

        this.warp = Domain.warp(Source.PERLIN, seed.next(), 80, 2, 50).warp(Domain.direction(
                Source.simplex(seed.next(), tectonicScale, 3),
                Source.constant(continentScale * 0.65)
        ));

        this.shape = Source.simplex(seed.next(), settings.continent.continentScale * 2, 1)
                .bias(0.65).clamp(0, 1);
    }

    @Override
    public float getValue(float x, float y) {
        Cell cell = new Cell();
        apply(cell, x, y);
        return cell.continentEdge;
    }

    @Override
    public void apply(Cell cell, final float x, final float y) {
        // apply warping to input coords
        float ox = warp.getOffsetX(x, y);
        float oz = warp.getOffsetY(x, y);

        float px = x + ox;
        float py = y + oz;

        px *= frequency;
        py *= frequency;

        // combined calc of voronoi distance, identity and coordinates
        int cellX = 0;
        int cellY = 0;
        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        Vec2f center = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xr, yr) & 255];

        float edgeDistance = NumConstants.LARGE;
        float edgeDistance2 = NumConstants.LARGE;
        float valueDistance = NumConstants.LARGE;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cx = xr + dx;
                int cy = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, cx, cy) & 255];

                float vecX = cx - px + vec.x;
                float vecY = cy - py + vec.y;
                float distance = distanceFunc.apply(vecX, vecY);

                if (distance < valueDistance) {
                    valueDistance = distance;
                    cellX = cx;
                    cellY = cy;
                    center = vec;
                }

                if (distance < edgeDistance2) {
                    edgeDistance2 = Math.max(edgeDistance, distance);
                } else {
                    edgeDistance2 = Math.max(edgeDistance, edgeDistance2);
                }

                edgeDistance = Math.min(edgeDistance, distance);
            }
        }

        // apply to the input cell
        cell.continentIdentity = cellIdentity(seed, cellX, cellY);
        cell.continentEdge = cellEdgeValue(edgeDistance, edgeDistance2);
        cell.continentX = (int) ((cellX + center.x) / frequency);
        cell.continentZ = (int) ((cellY + center.y) / frequency);

        // apply the 'shape' distortion to the edge noise. this produces cliffs
        cell.continentEdge *= getShape(x, y, cell.continentEdge);
    }

    @Override
    public final float getEdgeNoise(float x, float y) {
        float ox = warp.getOffsetX(x, y);
        float oz = warp.getOffsetY(x, y);

        float px = x + ox;
        float py = y + oz;

        px *= frequency;
        py *= frequency;

        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float edgeDistance = NumConstants.LARGE;
        float edgeDistance2 = NumConstants.LARGE;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = distanceFunc.apply(vecX, vecY);

                if (distance < edgeDistance2) {
                    edgeDistance2 = Math.max(edgeDistance, distance);
                } else {
                    edgeDistance2 = Math.max(edgeDistance, edgeDistance2);
                }

                edgeDistance = Math.min(edgeDistance, distance);
            }
        }

        float edgeValue = cellEdgeValue(edgeDistance, edgeDistance2);
        float shapeNoise = getShape(x, y, edgeValue);
        return edgeValue * shapeNoise;
    }

    @Override
    public void getNearestCenter(float x, float z, MutableVeci pos) {
        float ox = warp.getOffsetX(x, z);
        float oz = warp.getOffsetY(x, z);

        float px = x + ox;
        float py = z + oz;

        px *= frequency;
        py *= frequency;

        int cellX = 0;
        int cellY = 0;
        Vec2f center = null;

        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float valueDistance = NumConstants.LARGE;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = distanceFunc.apply(vecX, vecY);

                if (distance < valueDistance || (center == null && dx == 0 && dy == 0)) {
                    valueDistance = distance;
                    center = vec;
                    cellX = xi;
                    cellY = yi;
                }
            }
        }

        pos.x = (int) ((cellX + center.x) / frequency);
        pos.z = (int) ((cellY + center.y) / frequency);
    }

    @Override
    public float getDistanceToOcean(int cx, int cz, float dx, float dz, MutableVeci pos) {
        float high = getDistanceToEdge(cx, cz, dx, dz, pos);
        float low = 0;

        // perform a binary search between cx,cz and the cell's edges to find the
        // point where ocean becomes land
        for (int i = 0; i < 50; i++) {
            float mid = (low + high) / 2F;
            float x = cx + dx * mid;
            float z = cz + dz * mid;
            float edge = getEdgeNoise(x, z);

            if (edge > controlPoints.shallowOcean) {
                low = mid;
            } else {
                high = mid;
            }

            if (high - low < 10) {
                break;
            }
        }
        return high;
    }

    @Override
    public float getDistanceToEdge(int cx, int cz, float dx, float dz, MutableVeci pos) {
        float distance = continentScale * 4;

        for (int i = 0; i < 10; i++) {
            // check positions in direction dx,dz away from the continent center cx,cz
            // until we hit a new continent (the continent center coords will be different)
            float x = cx + dx * distance;
            float z = cz + dz * distance;
            getNearestCenter(x, z, pos);

            // increment the distance in large steps
            distance += distance;

            if (pos.x != cx || pos.z != cz) {
                // perform a binary search between continent center and x,z to find the
                // border point between the two continent cells

                float low = 0;
                float high = distance;

                for (int j = 0; j < 50; j++) {
                    float mid = (low + high) / 2F;
                    float px = cx + dx * mid;
                    float pz = cz + dz * mid;
                    getNearestCenter(px, pz, pos);

                    if (pos.x == cx && pos.z == cz) {
                        low = mid;
                    } else {
                        high = mid;
                    }

                    // within 50 seams to be accurate enough
                    if (high - low < 50) {
                        break;
                    }
                }
                return high;
            }
        }

        return distance;
    }

    protected float cellIdentity(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1, 1, 2);
    }

    protected float cellEdgeValue(float distance, float distance2) {
        EdgeFunc edge = EdgeFunc.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        value = 1F - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        if (value <= clampMin) {
            return 0F;
        }
        if (value >= clampMax) {
            return 1F;
        }
        return (value - clampMin) / clampRange;
    }

    // shape 'cuts' out some of the coast line to create sharper descents into the ocean (cliffs)
    // only apply to coastal areas and below
    protected float getShape(float x, float z, float edgeValue) {
        if (edgeValue >= controlPoints.inland) {
            return 1F;
        } else {
            // does this make sense ?!
            float alpha = (edgeValue / controlPoints.inland);
            return shape.getValue(x, z) * alpha;
        }
    }
}
