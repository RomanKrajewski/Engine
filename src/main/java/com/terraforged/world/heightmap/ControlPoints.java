package com.terraforged.world.heightmap;

import com.terraforged.core.settings.WorldSettings;

public class ControlPoints {

    public final float deepOcean;
    public final float shallowOcean;
    public final float beach;
    public final float coast;
    public final float coastMarker;
    public final float inland;

    public ControlPoints(WorldSettings.ControlPoints points) {
        if (!validate(points)) {
            points = new WorldSettings.ControlPoints();
        }

        this.inland = points.inland;
        this.coast = points.coast;
        this.beach = points.beach;
        this.shallowOcean = points.shallowOcean;
        this.deepOcean = points.deepOcean;
        this.coastMarker = coast + ((inland - coast) / 2F);
    }

    public static boolean validate(WorldSettings.ControlPoints points) {
        return points.inland <= 1
                && points.inland > points.coast
                && points.coast > points.beach
                && points.beach > points.shallowOcean
                && points.shallowOcean > points.deepOcean
                && points.deepOcean >= 0;
    }
}
