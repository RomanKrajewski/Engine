package com.terraforged.world.rivermap.gen;

import com.terraforged.world.rivermap.river.River;

public class GenRiver implements Comparable<GenRiver> {

    public final float dx;
    public final float dz;
    public final float angle;
    public final float length;
    public final River river;

    public GenRiver(River river, float angle, float dx, float dz, float length) {
        this.dx = dx;
        this.dz = dz;
        this.river = river;
        this.angle = angle;
        this.length = length;
    }

    @Override
    public int compareTo(GenRiver o) {
        return Float.compare(angle, o.angle);
    }
}
