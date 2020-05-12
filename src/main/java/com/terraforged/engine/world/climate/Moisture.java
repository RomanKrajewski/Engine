package com.terraforged.engine.world.climate;

import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;

public class Moisture implements Module {

    private final Module source;
    private final int power;

    public Moisture(int seed, int scale, int power) {
        this(Source.simplex(seed, scale, 1).clamp(0.125, 0.875).map(0, 1), power);
    }

    public Moisture(Module source, int power) {
        this.source = source.freq(0.5, 1);
        this.power = power;
    }

    @Override
    public float getValue(float x, float y) {
        float noise = source.getValue(x, y);
        if (power < 2) {
            return noise;
        }

        noise = (noise - 0.5F) * 2F;

        float value = NoiseUtil.pow(noise, power);
        value = NoiseUtil.copySign(value, noise);

        return NoiseUtil.map(value, -1, 1, 2);
    }
}
