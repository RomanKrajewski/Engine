/*
 *   
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

package com.terraforged.world.rivermap.river;

import com.terraforged.world.heightmap.Levels;
import me.dags.noise.util.NoiseUtil;

public class RiverConfig {

    public final int order;
    public final boolean main;
    public final int bedWidth;
    public final int bankWidth;
    public final float bedHeight;
    public final float minBankHeight;
    public final float maxBankHeight;
    public final int length;
    public final int length2;
    public final double fade;

    private RiverConfig(Builder builder) {
        main = builder.main;
        order = builder.order;
        bedWidth = builder.bedWidth;
        bankWidth = builder.bankWidth;
        bedHeight = builder.levels.water(-builder.bedDepth);
        minBankHeight = builder.levels.water(builder.minBankHeight);
        maxBankHeight = builder.levels.water(builder.maxBankHeight);
        length = builder.length;
        length2 = builder.length * builder.length;
        fade = builder.fade;
    }

    private RiverConfig(boolean main, int order, int bedWidth, int bankWidth, float bedHeight, float minBankHeight, float maxBankHeight, int length, int length2, double fade) {
        this.main = main;
        this.order = order;
        this.bedWidth = bedWidth;
        this.bankWidth = bankWidth;
        this.bedHeight = bedHeight;
        this.minBankHeight = minBankHeight;
        this.maxBankHeight = maxBankHeight;
        this.length = length;
        this.length2 = length2;
        this.fade = fade;
    }

    public RiverConfig createFork(float connectWidth) {
        if (bankWidth < connectWidth) {
            return this;
        }

        float scale = bankWidth / connectWidth;
        return new RiverConfig(
                false,
                order + 1,
                NoiseUtil.round(bedWidth / scale),
                NoiseUtil.round(bankWidth / scale),
                bedHeight,
                minBankHeight,
                maxBankHeight,
                length,
                length2,
                fade
        );
    }

    public static Builder builder(Levels levels) {
        return new Builder(levels);
    }

    public static class Builder {

        private boolean main = false;
        private int order = 0;
        private int bedWidth = 4;
        private int bankWidth = 15;
        private int bedDepth = 5;
        private int maxBankHeight = 1;
        private int minBankHeight = 1;
        private int length = 1000;
        private double fade = 0.2;
        private final Levels levels;

        private Builder(Levels levels) {
            this.levels = levels;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder main(boolean value) {
            this.main = value;
            return this;
        }

        public Builder bedWidth(int value) {
            this.bedWidth = value;
            return this;
        }

        public Builder bankWidth(int value) {
            this.bankWidth = value;
            return this;
        }

        public Builder bedDepth(int depth) {
            this.bedDepth = depth;
            return this;
        }

        public Builder bankHeight(int min, int max) {
            this.minBankHeight = Math.min(min, max);
            this.maxBankHeight = Math.max(min, max);
            return this;
        }

        public Builder length(int value) {
            this.length = value;
            return this;
        }

        public Builder fade(double value) {
            this.fade = value;
            return this;
        }

        public RiverConfig build() {
            return new RiverConfig(this);
        }
    }
}
