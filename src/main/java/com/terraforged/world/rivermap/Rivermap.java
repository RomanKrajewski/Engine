package com.terraforged.world.rivermap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.cache.ExpiringEntry;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.rivermap.lake.Lake;
import com.terraforged.world.rivermap.river.River;
import me.dags.noise.domain.Domain;

import java.util.List;

public class Rivermap implements ExpiringEntry {

    private final int x;
    private final int z;
    private final Domain lakeWarp;
    private final Domain riverWarp;
    private final List<Lake> lakes;
    private final List<River> rivers;
    private final long timestamp = System.currentTimeMillis();

    public Rivermap(int x, int z, int seed, List<River> rivers, List<Lake> lakes) {
        this.x = x;
        this.z = z;
        this.lakes = lakes;
        this.rivers = rivers;
        this.lakeWarp = Domain.warp(++seed, 200, 1, 400)
                .add(Domain.warp(++seed, 50, 2, 50));
        this.riverWarp = Domain.warp(++seed, 400, 1, 400)
                .add(Domain.warp(++seed, 80, 1, 35))
                .add(Domain.warp(++seed, 16, 1, 2))
        ;
    }

    public void apply(Cell cell, float x, float z) {
        float rx = riverWarp.getX(x, z);
        float rz = riverWarp.getY(x, z);
        for (River r : getRivers()) {
            r.apply(cell, rx, rz);
        }

        float lx = lakeWarp.getX(x, z);
        float lz = lakeWarp.getY(x, z);
        for (Lake l : getLakes()) {
            l.apply(cell, lx, lz);
        }
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public List<River> getRivers() {
        return rivers;
    }

    public List<Lake> getLakes() {
        return lakes;
    }

    public static Rivermap get(Cell cell, Rivermap instance, Heightmap heightmap) {
        return get(cell.continentX, cell.continentZ, instance, heightmap);
    }

    public static Rivermap get(int x, int z, Rivermap instance, Heightmap heightmap) {
        if (instance != null && x == instance.getX() && z == instance.getZ()) {
            return instance;
        }
        return heightmap.getRivers().getRivers(x, z);
    }
}