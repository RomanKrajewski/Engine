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

package com.terraforged.core.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;
import com.terraforged.world.terrain.populator.TerrainPopulator;
import com.terraforged.n2d.Module;

@Serializable
public class TerrainSettings {

    public General general = new General();
    public Terrain steppe = new Terrain(3F, 1F, 1F);
    public Terrain plains = new Terrain(3F, 1F, 1F);
    public Terrain hills = new Terrain(2F, 1F, 1F);
    public Terrain dales = new Terrain(2F, 1F, 1F);
    public Terrain plateau = new Terrain(2F, 1F, 1F);
    public Terrain badlands = new Terrain(2F, 1F, 1F);
    public Terrain torridonian = new Terrain(0.5F, 1F, 1F);
    public Terrain mountains = new Terrain(2F, 1F, 1F);
    public Terrain volcano = new Terrain(1F, 1F, 1F);

    @Serializable
    public static class General {

        @Range(min = 125, max = 5000)
        @Comment("Controls the size of terrain regions")
        public int terrainRegionSize = 1200;

        @Range(min = 0, max = 1)
        @Comment("Globally controls the vertical scaling of terrain")
        public float globalVerticalScale = 0.98F;

        @Range(min = 0, max = 5)
        @Comment("Globally controls the horizontal scaling of terrain")
        public float globalHorizontalScale = 1.0F;
    }

    @Serializable
    public static class Terrain {

        @Range(min = 0, max = 10)
        @Comment("Controls how common this terrain type is")
        public float weight = 1F;

        @Range(min = 0, max = 2)
        @Comment("Controls the base height of this terrain")
        public float baseScale = 1F;

        @Range(min = 0F, max = 10F)
        @Comment("Stretches or compresses the terrain vertically")
        public float verticalScale = 1F;

        @Range(min = 0F, max = 10F)
        @Comment("Stretches or compresses the terrain horizontally")
        public float horizontalScale = 1F;

        public Terrain() {

        }

        public Terrain(float weight, float vertical, float horizontal) {
            this.weight = weight;
            this.verticalScale = vertical;
            this.horizontalScale = horizontal;
        }

        public Module apply(double bias, double scale, Module module) {
            double moduleBias = bias * baseScale;
            double moduleScale = scale * verticalScale;
            Module outputModule = module.scale(moduleScale).bias(moduleBias);
            return TerrainPopulator.clamp(outputModule);
        }
    }
}
