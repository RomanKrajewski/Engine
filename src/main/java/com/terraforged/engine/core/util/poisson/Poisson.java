package com.terraforged.engine.core.util.poisson;

import com.terraforged.engine.core.concurrent.ObjectPool;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;

import java.util.Arrays;

public class Poisson {

    private static final int SAMPLES = 50;

    private final int radius;
    private final int radius2;
    private final float halfRadius;
    private final int maxDistance;
    private final int regionSize;
    private final int gridSize;
    private final float cellSize;
    private final ObjectPool<Vec2f[][]> pool;

    public Poisson(int radius) {
        int size = 48;
        this.radius = radius;
        this.radius2 = radius * radius;
        this.halfRadius = radius / 2F;
        this.maxDistance = radius * 2;
        this.regionSize = size - radius;
        this.cellSize = radius / NoiseUtil.SQRT2;
        this.gridSize = (int) Math.ceil(regionSize / cellSize);
        this.pool = new ObjectPool<>(3, () -> new Vec2f[gridSize][gridSize]);
    }

    public int getRadius() {
        return radius;
    }

    public void visit(int chunkX, int chunkZ, PoissonContext context, Visitor visitor) {
        try (ObjectPool.Item<Vec2f[][]> grid = pool.get()) {
            clear(grid.getValue());
            context.startX = (chunkX << 4);
            context.startZ = (chunkZ << 4);
            context.endX = context.startX + 16;
            context.endZ = context.startZ + 16;
            int regionX = (context.startX >> 5);
            int regionZ = (context.startZ >> 5);
            context.offsetX = regionX << 5;
            context.offsetZ = regionZ << 5;
            context.random.setSeed(NoiseUtil.hash2D(context.seed, regionX, regionZ));
            int x = context.random.nextInt(regionSize);
            int z = context.random.nextInt(regionSize);
            visit(x, z, grid.getValue(), SAMPLES, context, visitor);
        }
    }

    private void visit(float px, float pz, Vec2f[][] grid, int samples, PoissonContext context, Visitor visitor) {
        for (int i = 0; i < samples; i++) {
            float angle = context.random.nextFloat() * NoiseUtil.PI2;
            float distance = radius + (context.random.nextFloat() * maxDistance);
            float x = halfRadius + px + NoiseUtil.sin(angle) * distance;
            float z = halfRadius + pz + NoiseUtil.cos(angle) * distance;
            if (valid(x, z, grid, context)) {
                Vec2f vec = new Vec2f(x, z);
                visit(vec, context, visitor);
                int cellX = (int) (x / cellSize);
                int cellZ = (int) (z / cellSize);
                grid[cellZ][cellX] = vec;
                visit(vec.x, vec.y, grid, samples, context, visitor);
            }
        }
    }

    private void visit(Vec2f pos, PoissonContext context, Visitor visitor) {
        int dx = context.offsetX + (int) pos.x;
        int dz = context.offsetZ + (int) pos.y;
        if (dx >= context.startX && dx < context.endX && dz >= context.startZ && dz < context.endZ) {
            visitor.visit(dx, dz);
        }
    }

    private boolean valid(float x, float z, Vec2f[][] grid, PoissonContext context) {
        if (x < 0 || x >= regionSize || z < 0 || z >= regionSize) {
            return false;
        }

        int cellX = (int) (x / cellSize);
        int cellZ = (int) (z / cellSize);
        if (grid[cellZ][cellX] != null) {
            return false;
        }

        float noise = context.density.getValue(context.offsetX + x, context.offsetZ + z);
        float radius2 = noise * this.radius2;

        int searchRadius = 2;
        int minX = Math.max(0, cellX - searchRadius);
        int maxX = Math.min(grid[0].length - 1, cellX + searchRadius);
        int minZ = Math.max(0, cellZ - searchRadius);
        int maxZ = Math.min(grid.length - 1, cellZ + searchRadius);

        for (int dz = minZ; dz <= maxZ; dz++) {
            for (int dx = minX; dx <= maxX; dx++) {
                Vec2f vec = grid[dz][dx];
                if (vec == null) {
                    continue;
                }

                float dist2 = vec.dist2(x, z);
                if (dist2 < radius2) {
                    return false;
                }
            }
        }
        return true;
    }

    public interface Visitor {

        void visit(int x, int z);
    }

    private static void clear(Vec2f[][] grid) {
        for (Vec2f[] row : grid) {
            Arrays.fill(row, null);
        }
    }
}
