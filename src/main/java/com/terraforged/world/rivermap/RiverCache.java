package com.terraforged.world.rivermap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.cache.Cache;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.rivermap.gen.RiverGenerator;

import java.util.concurrent.TimeUnit;

public class RiverCache {

    private final RiverGenerator generator;
    private final Cache<Rivermap> cache = new Cache<>(5, 1, TimeUnit.MINUTES);

    public RiverCache(Heightmap heightmap, GeneratorContext context) {
        this.generator = new RiverGenerator(heightmap, context);
    }

    public Rivermap getRivers(Cell cell) {
        return getRivers(cell.continentX, cell.continentZ);
    }

    public Rivermap getRivers(int x, int z) {
        return cache.computeIfAbsent(NoiseUtil.seed(x, z), id -> generator.compute(x, z, id));
    }
}
