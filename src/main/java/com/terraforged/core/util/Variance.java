package com.terraforged.core.util;

import java.util.Random;

public class Variance {

    private final float min;
    private final float range;

    private Variance(float min, float range) {
        this.min = min;
        this.range = range;
    }

    public float next(Random random) {
        return min + random.nextFloat() * range;
    }

    public float next(Random random, float scalar) {
        return next(random) * scalar;
    }

    public static Variance min(double min) {
        return new Variance((float) min, 1F - (float) min);
    }

    public static Variance range(double range) {
        return new Variance(1 - (float) range, (float) range);
    }

    public static Variance of(double min, double range) {
        return new Variance((float) min, (float) range);
    }
}
