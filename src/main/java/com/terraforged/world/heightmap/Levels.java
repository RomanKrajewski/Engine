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

package com.terraforged.world.heightmap;

import com.terraforged.core.settings.Settings;
import com.terraforged.core.settings.WorldSettings;
import com.terraforged.n2d.util.NoiseUtil;

public class Levels {

    public final int worldHeight;

    public final float unit;

    // y index of the top-most water block
    public final int waterY;
    private final int groundY;

    // top of the first ground block (ie 1 above ground index)
    public final int groundLevel;
    // top of the top-most water block (ie 1 above water index)
    public final int waterLevel;

    // ground index mapped between 0-1 (default 63 / 256)
    public final float ground;
    // water index mapped between 0-1 (default 62 / 256)
    public final float water;

    private final float elevationRange;

    public Levels(WorldSettings settings) {
        this(settings.properties.worldHeight, settings.properties.seaLevel);
    }

    public Levels(int height, int seaLevel) {
        this.worldHeight = Math.max(1, height);
        unit = NoiseUtil.div(1, worldHeight);

        waterLevel = seaLevel;
        groundLevel = waterLevel + 1;

        waterY = Math.min(waterLevel - 1, worldHeight);
        groundY = Math.min(groundLevel - 1, worldHeight);

        ground = NoiseUtil.div(groundY, worldHeight);
        water = NoiseUtil.div(waterY, worldHeight);
        elevationRange = 1F - water;
    }

    public int scale(float value) {
        if (value >= 1F) {
            return worldHeight - 1;
        }
        return (int) (value * worldHeight);
    }

    public float elevation(float value) {
        if (value <= water) {
            return 0F;
        }
        return (value - water) / elevationRange;
    }

    public float elevation(int y) {
        if (y <= waterY) {
            return 0F;
        }
        return scale(y - waterY) / elevationRange;
    }

    public float scale(int level) {
        return NoiseUtil.div(level, worldHeight);
    }

    public float water(int amount) {
        return NoiseUtil.div(waterY + amount, worldHeight);
    }

    public float ground(int amount) {
        return NoiseUtil.div(groundY + amount, worldHeight);
    }

    public static float scale(int steps, Settings settings) {
        return steps / (float) settings.world.properties.worldHeight;
    }
}
