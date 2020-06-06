package com.terraforged.core.region.gen;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.pool.ArrayPool;
import com.terraforged.core.region.Region;

public class RegionResources {

    public final ArrayPool<Cell> blocks = ArrayPool.of(100, Cell[]::new);
    public final ArrayPool<Region.GenChunk> chunks = ArrayPool.of(100, Region.GenChunk[]::new);
}
