package com.terraforged.world.rivermap.wetland;

import com.terraforged.core.settings.RiverSettings;
import com.terraforged.core.util.Variance;
import com.terraforged.n2d.util.NoiseUtil;

public class WetlandConfig {

    public final int skipSize;
    public final Variance length;
    public final Variance width;

    public WetlandConfig(RiverSettings.Wetland settings) {
        skipSize = Math.max(1, NoiseUtil.round((1 - settings.chance) * 10));
        length = Variance.of(settings.sizeMin, settings.sizeMax);
        width  = Variance.of(Wetland.WIDTH_MIN, Wetland.WIDTH_MAX);
    }
}
