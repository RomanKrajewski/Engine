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

package com.terraforged.core.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;

@Serializable
public class RiverSettings {

    /**
     * RIVER PROPERTIES
     */
    @Range(min = 0, max = 30)
    @Comment("Controls the number of main rivers per continent")
    public int riverCount = 13;

    public River mainRivers = new River(5, 2, 8,250, 20, 8, 0.95F);

    public River branchRivers = new River(4, 1, 6,100, 14, 5, 0.95F);

    public Lake lakes = new Lake();

    public Wetland wetlands = new Wetland();

    @Serializable
    public static class River {

        @Range(min = 1, max = 10)
        @Comment("Controls the depth of the river")
        public int bedDepth;

        @Range(min = 5, max = 10)
        @Comment("Controls the height of river banks")
        public int minBankHeight;

        @Range(min = 8, max = 10)
        @Comment("Controls the height of river banks")
        public int maxBankHeight;

        @Range(min = 1, max = 20)
        @Comment("Controls the river-bed width")
        public int bedWidth;

        @Range(min = 1, max = 50)
        @Comment("Controls the river-banks width")
        public int bankWidth;

        @Range(min = 0.0F, max = 1.0F)
        @Comment("Controls how much rivers taper")
        public float fade;

        @Range(min = 200, max = 300)
        @Comment("Controls the river valley width")
        public int valleyWidth;

        public River() {
        }

        public River(int depth, int minBank, int maxBank, int valleyWidth, int outer, int inner, float fade) {
            this.minBankHeight = minBank;
            this.maxBankHeight = maxBank;
            this.bankWidth = outer;
            this.bedWidth = inner;
            this.bedDepth = depth;
            this.fade = fade;
            this.valleyWidth = valleyWidth;
        }
    }

    @Serializable
    public static class Lake {

        @Range(min = 0.0F, max = 1.0F)
        @Comment("Controls the chance of a lake spawning")
        public float chance = 0.3F;

        @Range(min = 0F, max = 1F)
        @Comment("The minimum distance along a river that a lake will spawn")
        public float minStartDistance = 0.0F;

        @Range(min = 0F, max = 1F)
        @Comment("The maximum distance along a river that a lake will spawn")
        public float maxStartDistance = 0.03F;

        @Range(min = 1, max = 20)
        @Comment("The max depth of the lake")
        public int depth = 10;

        @Range(min = 10, max = 100)
        @Comment("The minimum size of the lake")
        public int sizeMin = 75;

        @Range(min = 50, max = 500)
        @Comment("The maximum size of the lake")
        public int sizeMax = 150;

        @Range(min = 1, max = 10)
        @Comment("The minimum bank height")
        public int minBankHeight = 2;

        @Range(min = 1, max = 10)
        @Comment("The maximum bank height")
        public int maxBankHeight = 10;

        public Lake() {

        }
    }

    @Serializable
    public static class Wetland {

        @Range(min = 0.0F, max = 1.0F)
        @Comment("Controls how common wetlands are")
        public float chance = 0.5F;

        @Range(min = 50, max = 500)
        @Comment("The minimum size of the wetlands")
        public int sizeMin = 175;

        @Range(min = 50, max = 500)
        @Comment("The maximum size of the wetlands")
        public int sizeMax = 225;
    }
}
