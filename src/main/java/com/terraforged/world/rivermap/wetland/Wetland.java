package com.terraforged.world.rivermap.wetland;

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
    private final float mounds;
    private final Module shape;

    public Wetland(Vec2f a, Vec2f b, float radius, Levels levels, Terrains terrains) {
        super(terrains.wetlands, Source.ZERO, Source.ZERO);
        this.bed = levels.water(-2);
        this.banks = levels.ground(4);
        this.mounds = levels.water(3);
        this.line = Source.line(a.x, a.y, b.x, b.y, radius, 0, 0);
        this.shape = Source.perlin(123, 8, 1).clamp(0.4, 0.6).map(0, 1);
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

        if (cell.value >= bed && cell.value <= mounds) {
            float shapeAlpha = shape.getValue(x, z) * poolsAlpha;
            cell.value = NoiseUtil.lerp(cell.value, mounds, shapeAlpha);
        }

        cell.terrain = getType();
        cell.riverMask *= (1 - valleyAlpha);
    }
}
