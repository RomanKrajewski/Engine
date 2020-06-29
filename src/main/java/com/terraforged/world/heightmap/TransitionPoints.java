package com.terraforged.world.heightmap;

import com.terraforged.core.settings.WorldSettings;

public class TransitionPoints {

    public final float deepOcean;
    public final float shallowOcean;
    public final float beach;
    public final float coast;
    public final float coastMarker;
    public final float inland;

    public TransitionPoints(WorldSettings.TransitionPoints points) {
        if (!validate(points)) {
            points = new WorldSettings.TransitionPoints();
        }

        this.inland = points.inland;
        this.coast = points.coast;
        this.beach = points.beach;
        this.shallowOcean = points.shallowOcean;
        this.deepOcean = points.deepOcean;
        this.coastMarker = coast + ((inland - coast) / 2F);
    }

    public static boolean validate(WorldSettings.TransitionPoints points) {
        return points.inland <= 1
                && points.inland > points.coast
                && points.coast > points.beach
                && points.beach > points.shallowOcean
                && points.shallowOcean > points.deepOcean
                && points.deepOcean >= 0;
    }
}
