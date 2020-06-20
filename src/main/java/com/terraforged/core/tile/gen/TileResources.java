package com.terraforged.core.tile.gen;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.pool.ArrayPool;
import com.terraforged.core.tile.Tile;

public class TileResources {

    public final ArrayPool<Cell> blocks = ArrayPool.of(100, Cell[]::new);
    public final ArrayPool<Tile.GenChunk> chunks = ArrayPool.of(100, Tile.GenChunk[]::new);
}
