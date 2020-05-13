package com.terraforged.world.heightmap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.Region;
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

    public Cell getCell(int x, int z) {
        Cell cell = new Cell();
        applyCell(cell, x, z);
        return cell;
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
                return true;
            }
        }
        return false;
    }

    private void compute(Cell cell, int x, int z) {
        heightmap.apply(cell, x, z);

        // approximation - actual beaches depend on steepness but that's too expensive to calculate
        if (cell.terrainType == context.terrain.coast && cell.value > waterLevel && cell.value <= beachLevel) {
            cell.terrainType = context.terrain.beach;
        }

        for (Decorator decorator : decorators.getDecorators()) {
            if (decorator.apply(cell, x, z)) {
                break;
            }
        }
    }
}
