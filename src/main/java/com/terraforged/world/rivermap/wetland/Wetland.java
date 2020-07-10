package com.terraforged.world.rivermap.wetland;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.source.Line;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.n2d.util.Vec2f;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;
import com.terraforged.world.terrain.populator.TerrainPopulator;

public class Wetland extends TerrainPopulator {

    public static final float WIDTH_MIN = 50F;
    public static final float WIDTH_MAX = 150F;

    private static final float VALLEY = 0.6F;
    private static final float POOLS = 0.7F;
    private static final float BANKS = POOLS - VALLEY;

    private final Line line;
    private final float bed;
    private final float banks;
    private final float moundMin;
    private final float moundMax;
    private final float moundVariance;
    private final Module moundShape;
    private final Module moundHeight;
    private final Module terrainEdge;

    public Wetland(Seed seed, Vec2f a, Vec2f b, float radius, Levels levels, Terrains terrains) {
        super(terrains.wetlands, Source.ZERO, Source.ZERO);
        this.bed = levels.water(-1) - (0.5F / levels.worldHeight);
        this.banks = levels.ground(4);
        this.moundMin = levels.water(1);
        this.moundMax = levels.water(2);
        this.moundVariance = moundMax - moundMin;
        this.line = Source.line(a.x, a.y, b.x, b.y, radius, 0, 0);
        this.terrainEdge = Source.perlin(seed.next(), 8, 1).clamp(0.2, 0.8).map(0, 0.9);
        this.moundShape = Source.simplex(seed.next(), 12, 1).clamp(0.4, 0.6).map(0, 1);
        this.moundHeight = Source.simplex(seed.next(), 20, 1).clamp(0, 0.3).map(0, 1);
    }

    @Override
    public void apply(Cell cell, float x, float z) {

    }

    public void apply(Cell cell, float rx, float rz, float x, float z) {
        if (cell.value < bed) {
            return;
        }

        float dist = line.getValue(rx, rz);
        if (dist <= 0) {
            return;
        }

        float valleyAlpha = NoiseUtil.map(dist, 0, VALLEY, VALLEY);
        if (cell.value > banks) {
            cell.value = NoiseUtil.lerp(cell.value, banks, valleyAlpha);
        }

        float poolsAlpha = NoiseUtil.map(dist, VALLEY, POOLS, BANKS);
        if (cell.value > bed && cell.value <= banks) {
            cell.value = NoiseUtil.lerp(cell.value, bed, poolsAlpha);
        }

        if (poolsAlpha >= 1) {
            cell.erosionMask = true;
        }

        if (dist > VALLEY && poolsAlpha > terrainEdge.getValue(x, z)) {
            cell.terrain = getType();
        }

        if (cell.value >= bed && cell.value < moundMax) {
            float shapeAlpha = moundShape.getValue(x, z) * poolsAlpha;
            float mounds = moundMin + moundHeight.getValue(x, z) * moundVariance;
            cell.value = NoiseUtil.lerp(cell.value, mounds, shapeAlpha);
        }

        cell.riverMask *= (1 - valleyAlpha);
    }
}
