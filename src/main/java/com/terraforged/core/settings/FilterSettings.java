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
public class FilterSettings {

    public Erosion erosion = new Erosion();

    public Smoothing smoothing = new Smoothing();

    @Serializable
    public static class Erosion {

        @Range(min = 1000, max = 50000)
        @Comment("Controls the number of erosion iterations")
        public int iterations = 15000;

        @Range(min = 0F, max = 1F)
        @Comment("Controls how quickly material dissolves (during erosion)")
        public float erosionRate = 0.5F;

        @Range(min = 0F, max = 1F)
        @Comment("Controls how quickly material is deposited (during erosion)")
        public float depositeRate = 0.5F;

        @Range(min = 1, max = 50)
        @Comment("Controls the number of iterations that a single water droplet is simulated for")
        public int dropletLifetime = 25;

        @Range(min = 0F, max = 1F)
        @Comment("Controls the starting volume of water that a simulated water droplet carries")
        public float dropletVolume = 0.7F;

        @Range(min = 0.1F, max = 1F)
        @Comment("Controls the starting velocity of the simulated water droplet")
        public float dropletVelocity = 0.7F;

        public Erosion() {

        }

        public Erosion copy() {
            Erosion erosion = new Erosion();
            erosion.iterations = iterations;
            erosion.erosionRate = erosionRate;
            erosion.depositeRate = depositeRate;
            erosion.dropletLifetime = dropletLifetime;
            erosion.dropletVolume = dropletVolume;
            erosion.dropletVelocity = dropletVelocity;
            return erosion;
        }
    }

    @Serializable
    public static class Smoothing {

        @Range(min = 0, max = 5)
        @Comment("Controls the number of smoothing iterations")
        public int iterations = 1;

        @Range(min = 0, max = 5)
        @Comment("Controls the smoothing radius")
        public float smoothingRadius = 1.75F;

        @Range(min = 0, max = 1)
        @Comment("Controls how strongly smoothing is applied")
        public float smoothingRate = 0.85F;
    }
}
