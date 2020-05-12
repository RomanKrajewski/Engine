package com.terraforged.engine.world.rivermap;

import com.terraforged.engine.core.cell.Cell;
import com.terraforged.engine.core.concurrent.cache.Cache;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.Heightmap;
import com.terraforged.engine.world.rivermap.gen.RiverGenerator;
import me.dags.noise.util.NoiseUtil;

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
