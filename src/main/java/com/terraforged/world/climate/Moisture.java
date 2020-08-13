/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.world.climate;

import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.util.NoiseUtil;

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
