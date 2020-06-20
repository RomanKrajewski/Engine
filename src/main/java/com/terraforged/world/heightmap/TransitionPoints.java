package com.terraforged.world.heightmap;

import com.terraforged.core.settings.WorldSettings;

public class TransitionPoints {

    public final float deepOcean;
    public final float shallowOcean;
    public final float beach;
    public final float coast;
    public final float inland;

    public TransitionPoints(WorldSettings.TransitionPoints transitionPoints) {
        this.inland = transitionPoints.inland;
        this.coast = Math.min(transitionPoints.coast, this.inland);
        this.beach = Math.min(transitionPoints.beach, this.coast);
        this.shallowOcean = Math.min(transitionPoints.shallowOcean, this.coast);
        this.deepOcean = Math.min(transitionPoints.deepOcean, this.shallowOcean);
    }
}
