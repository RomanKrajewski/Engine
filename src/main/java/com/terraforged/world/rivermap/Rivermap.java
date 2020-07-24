package com.terraforged.world.rivermap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.cache.ExpiringEntry;
import com.terraforged.n2d.domain.Domain;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.rivermap.gen.GenWarp;
import com.terraforged.world.rivermap.lake.Lake;
import com.terraforged.world.rivermap.river.River;
import com.terraforged.world.rivermap.wetland.Wetland;

import java.util.List;

public class Rivermap implements ExpiringEntry {

    private final int x;
    private final int z;
    private final Domain lakeWarp;
    private final Domain riverWarp;
    private final List<Lake> lakes;
    private final List<River> rivers;
    private final List<Wetland> wetland;
    private final long timestamp = System.currentTimeMillis();

    public Rivermap(int x, int z, GenWarp warp, List<River> rivers, List<Lake> lakes, List<Wetland> wetland) {
        this.x = x;
        this.z = z;
        this.lakes = lakes;
        this.rivers = rivers;
        this.lakeWarp = warp.lake;
        this.riverWarp = warp.river;
        this.wetland = wetland;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void apply(Cell cell, float x, float z) {
        float rx = riverWarp.getX(x, z);
        float rz = riverWarp.getY(x, z);

        for (int i = 0; i < rivers.size(); i++) {
            rivers.get(i).apply(cell, rx, rz);
        }

        for (int i = 0; i < wetland.size(); i++) {
            wetland.get(i).apply(cell, rx, rz, x, z);
        }

        float lx = lakeWarp.getX(x, z);
        float lz = lakeWarp.getY(x, z);
        for (int i = 0; i < lakes.size(); i++) {
            lakes.get(i).apply(cell, lx, lz);
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
