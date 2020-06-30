package com.terraforged.world.continent.generator;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.WorldSettings;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.domain.Domain;
import com.terraforged.n2d.func.DistanceFunc;
import com.terraforged.n2d.func.EdgeFunc;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.world.continent.Continent;
import com.terraforged.world.continent.MutableVeci;
import com.terraforged.world.heightmap.TransitionPoints;

public abstract class AbstractContinentGenerator implements Continent {

    private static final float edgeClampMin = 0.1F;
    private static final float shapeClampMin = 0.4F;
    private static final float shapeClampMax = 0.6F;
    private static final float shapeRange = shapeClampMax - shapeClampMin;

    protected final int seed;
    protected final float frequency;
    protected final int continentScale;

    private final float edgeMin;
    private final float edgeMax;
    private final float edgeRange;

    private final DistanceFunc distanceFunc;
    private final TransitionPoints transition;

    protected final Domain warp;
    protected final Module shape;

    public AbstractContinentGenerator(Seed seed, WorldSettings settings) {
        int tectonicScale = settings.continent.continentScale * 4;

        this.continentScale = settings.continent.continentScale / 2;
        this.seed = seed.next();
        this.distanceFunc = settings.continent.continentShape;
        this.transition = new TransitionPoints(settings.transitionPoints);
        this.frequency = 1F / tectonicScale;
        this.edgeMin = 0.2F;
        this.edgeMax = 1.0F;
        this.edgeRange = edgeMax - edgeMin;

        this.warp = Domain.warp(Source.PERLIN, seed.next(), 20, 2, 20)
                .warp(Domain.warp(Source.SIMPLEX, seed.next(), continentScale, 3, continentScale));

        this.shape = Source.simplex(seed.next(), settings.continent.continentScale * 3, 1).bias(0.5).clamp(0, 1);
    }

    protected abstract void apply(Cell cell, float x, float y, float px, float py);

    protected abstract float getEdgeNoise(float x, float y, float px, float py);

    protected abstract void getContinentCenter(float px, float py, MutableVeci pos);

    @Override
    public DistanceFunc getDistFunc() {
        return distanceFunc;
    }

    @Override
    public float getValue(float x, float y) {
        Cell cell = new Cell();
        apply(cell, x, y);
        return cell.continentEdge;
    }

    @Override
    public final void apply(Cell cell, final float x, final float y) {
        float ox = warp.getOffsetX(x, y);
        float oz = warp.getOffsetY(x, y);

        float px = x + ox;
        float py = y + oz;

        px *= frequency;
        py *= frequency;

        this.apply(cell, x, y, px, py);
    }

    @Override
    public final float getEdgeNoise(float x, float y) {
        float ox = warp.getOffsetX(x, y);
        float oz = warp.getOffsetY(x, y);

        float px = x + ox;
        float py = y + oz;

        px *= frequency;
        py *= frequency;

        return getEdgeNoise(x, y, px, py);
    }

    @Override
    public final void getNearestCenter(float x, float z, MutableVeci pos) {
        float ox = warp.getOffsetX(x, z);
        float oz = warp.getOffsetY(x, z);

        float px = x + ox;
        float py = z + oz;

        px *= frequency;
        py *= frequency;

        getContinentCenter(px, py, pos);
    }

    @Override
    public float getDistanceToOcean(int cx, int cz, float dx, float dz, MutableVeci pos) {
        float high = getDistanceToEdge(cx, cz, dx, dz, pos);
        float low = 0;

        for (int i = 0; i < 50; i++) {
            float mid = (low + high) / 2F;
            float x = cx + dx * mid;
            float z = cz + dz * mid;
            float edge = getEdgeNoise(x, z);

            if (edge > transition.shallowOcean) {
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
            distance += distance;

            float x = cx + dx * distance;
            float z = cz + dz * distance;
            getNearestCenter(x, z, pos);

            if (pos.x != cx || pos.z != cz) {
                return getDistanceToEdge(cx, cz, dx, dz, distance, pos);
            }
        }

        return distance;
    }

    private float getDistanceToEdge(int cx, int cz, float dx, float dz, float distance, MutableVeci pos) {
        float low = 0;
        float high = distance;

        for (int i = 0; i < 50; i++) {
            float mid = (low + high) / 2F;
            float x = cx + dx * mid;
            float z = cz + dz * mid;
            getNearestCenter(x, z, pos);

            if (pos.x == cx && pos.z == cz) {
                low = mid;
            } else {
                high = mid;
            }

            if (high - low < 50) {
                break;
            }
        }
        return high;
    }

    protected float cellValue(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1, 1, 2);
    }

    protected float edgeValue(float distance, float distance2) {
        EdgeFunc edge = EdgeFunc.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        return 1 - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
    }

    protected float continentValue(float edgeValue) {
        if (edgeValue < edgeMin) {
            return 0F;
        }
        if (edgeValue > edgeMax) {
            return 1F;
        }
        return (edgeValue - edgeMin) / edgeRange;
    }

    // shape 'cuts' out some of the coast line to create sharper descents into the ocean (cliffs)
    protected float getShape(float x, float z, float edgeValue) {
        if (edgeValue >= transition.inland) {
            return 1F;
        }

        float alpha = (edgeValue / transition.inland);

        return shape.getValue(x, z) * alpha;
    }
}
