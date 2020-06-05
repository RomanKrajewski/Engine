package com.terraforged.world.heightmap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.region.Region;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.region.gen.RegionCache;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.WorldDecorators;
import com.terraforged.world.WorldGeneratorFactory;
import com.terraforged.world.terrain.decorator.Decorator;

public class WorldLookup {

    private final float waterLevel;
    private final float beachLevel;
    private final RegionCache cache;
    private final Heightmap heightmap;
    private final GeneratorContext context;
    private final WorldDecorators decorators;

    public WorldLookup(WorldGeneratorFactory factory, GeneratorContext context) {
        this.context = context;
        this.cache = context.cache;
        this.heightmap = factory.getHeightmap();
        this.decorators = factory.getDecorators();
        this.waterLevel = context.levels.water;
        this.beachLevel = context.levels.water(5);
    }

    public Resource<Cell> get(int x, int z) {
        ChunkReader chunk = cache.getChunk(x >> 4, z >> 4);
        Resource<Cell> cell = Cell.pooled();
        cell.get().copy(chunk.getCell(x & 15, z & 15));
        return cell;
    }

    public Resource<Cell> getCell(int x, int z) {
        Resource<Cell> resource = Cell.pooled();
        applyCell(resource.get(), x, z);
        return resource;
    }

    public void applyCell(Cell cell, int x, int z) {
        if (!computeCached(cell, x, z)) {
            compute(cell, x, z);
        }
    }

    private boolean computeCached(Cell cell, int x, int z) {
        int rx = cache.chunkToRegion(x >> 4);
        int rz = cache.chunkToRegion(z >> 4);
        Region region = cache.getIfPresent(rx, rz);
        if (region != null) {
            Cell c = region.getCell(x, z);
            if (c != null) {
                cell.copy(c);
            }
            return cell.terrain != null;
        }
        return false;
    }

    private void compute(Cell cell, int x, int z) {
        heightmap.apply(cell, x, z);

        // approximation - actual beaches depend on steepness but that's too expensive to calculate
        if (cell.terrain == context.terrain.coast && cell.value > waterLevel && cell.value <= beachLevel) {
            cell.terrain = context.terrain.beach;
        }

        for (Decorator decorator : decorators.getDecorators()) {
            if (decorator.apply(cell, x, z)) {
                break;
            }
        }
    }
}
