package com.terraforged.world.continent.generator;

import com.terraforged.core.NumConstants;
import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.WorldSettings;
import com.terraforged.world.continent.MutableVeci;
import me.dags.noise.func.DistanceFunc;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;
import me.dags.noise.util.Vec2i;

public class SingleContinentGenerator extends AbstractContinentGenerator {

    private final Vec2i center;

    public SingleContinentGenerator(Seed seed, WorldSettings settings) {
        super(seed, settings);
        this.center = getCenter(0, 0);
    }

    @Override
    protected void apply(Cell cell, float x, float y, float px, float py) {
        int cellX = 0;
        int cellY = 0;
        Vec2f center = null;

        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float edgeDistance = NumConstants.LARGE;
        float edgeDistance2 = NumConstants.LARGE;
        float valueDistance = NumConstants.LARGE;
        DistanceFunc dist = getDistFunc();

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < valueDistance) {
                    valueDistance = distance;
                    cellX = xi;
                    cellY = yi;
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

        if (center == null || !isCenter(center, cellX, cellY)) {
            return;
        }

        float edgeValue = edgeValue(edgeDistance, edgeDistance2);
        float shapeNoise = getShape(x, y, edgeValue);
        float continentNoise = continentValue(edgeValue);
        cell.continent = cellValue(seed, cellX, cellY);
        cell.continentEdge = shapeNoise * continentNoise;
        cell.continentX = (int) ((cellX + center.x) / frequency);
        cell.continentZ = (int) ((cellY + center.y) / frequency);
    }

    @Override
    protected void getContinentCenter(float px, float py, MutableVeci pos) {
        int cellX = 0;
        int cellY = 0;
        Vec2f center = null;

        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float valueDistance = NumConstants.LARGE;
        DistanceFunc dist = getDistFunc();

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < valueDistance) {
                    valueDistance = distance;
                    cellX = xi;
                    cellY = yi;
                    center = vec;
                }
            }
        }

        if (center == null) {
            return;
        }

        pos.x = (int) ((cellX + center.x) / frequency);
        pos.z = (int) ((cellY + center.y) / frequency);
    }

    @Override
    protected float getEdgeNoise(float x, float y, float px, float py) {
        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float edgeDistance = NumConstants.LARGE;
        float edgeDistance2 = NumConstants.LARGE;
        DistanceFunc dist = getDistFunc();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < edgeDistance2) {
                    edgeDistance2 = Math.max(edgeDistance, distance);
                } else {
                    edgeDistance2 = Math.max(edgeDistance, edgeDistance2);
                }

                edgeDistance = Math.min(edgeDistance, distance);
            }
        }
        float edgeValue = edgeValue(edgeDistance, edgeDistance2);
        float shapeNoise = getShape(x, y, edgeValue);
        return continentValue(edgeValue) * shapeNoise;
    }

    private boolean isCenter(Vec2f vec, int cellX, int cellZ) {
        int x = (int) ((cellX + vec.x) / frequency);
        if (x != center.x) {
            return false;
        }

        int y = (int) ((cellZ + vec.y) / frequency);
        return y == center.y;
    }

    private Vec2i getCenter(int px, int py) {
        MutableVeci pos = new MutableVeci();
        getNearestCenter(px, py, pos);
        return new Vec2i(pos.x, pos.z);
    }
}
