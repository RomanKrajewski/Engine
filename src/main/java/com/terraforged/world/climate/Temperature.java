package com.terraforged.world.climate;

import com.terraforged.n2d.Module;
import com.terraforged.n2d.util.NoiseUtil;

public class Temperature implements Module {

    private final int power;
    private final float frequency;

    public Temperature(float frequency, int power) {
        this.frequency = frequency;
        this.power = power;
    }

    @Override
    public float getValue(float x, float y) {
        y *= frequency;
        float sin = NoiseUtil.sin(y);
        sin = NoiseUtil.clamp(sin, -1, 1);

        float value = NoiseUtil.pow(sin, power);
        value = NoiseUtil.copySign(value, sin);

        return NoiseUtil.map(value, -1, 1, 2);
    }
}
