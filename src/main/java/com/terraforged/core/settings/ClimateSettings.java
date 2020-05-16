package com.terraforged.core.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;

@Serializable
public class ClimateSettings {

    public RangeValue temperature = new RangeValue(3, 0, 1F, 0.1F);

    public RangeValue moisture = new RangeValue(6, 2, 0, 1F, 0);

    public BiomeShape biomeShape = new BiomeShape();

    public BiomeNoise biomeEdgeShape = new BiomeNoise();

    @Serializable
    public static class RangeValue {

        @Range(min = 1, max = 20)
        @Comment("The horizontal scale")
        public int scale = 6;

        @Range(min = 1, max = 10)
        @Comment("How quickly values transition from an extremity")
        public int falloff = 2;

        @Range(min = 0F, max = 1F)
        @Comment("The lower limit of the range")
        public float min;

        @Range(min = 0F, max = 1F)
        @Comment("The upper limit of the range")
        public float max;

        @Range(min = -1F, max = 1F)
        @Comment("The bias towards either end of the range")
        public float bias = -0.1F;

        public RangeValue() {
            this(1, 0, 1, 0);
        }

        public RangeValue(int falloff, float min, float max, float bias) {
            this(6, falloff, min, max, bias);
        }

        public RangeValue(int scale, int falloff, float min, float max, float bias) {
            this.min = min;
            this.max = max;
            this.bias = bias;
            this.scale = scale;
            this.falloff = falloff;
        }

        public float getMin() {
            return NoiseUtil.clamp(Math.min(min, max), 0, 1);
        }

        public float getMax() {
            return NoiseUtil.clamp(Math.max(min, max), getMin(), 1);
        }

        public float getBias() {
            return NoiseUtil.clamp(bias, -1, 1);
        }

        public Module apply(Module module) {
            float min = getMin();
            float max = getMax();
            float bias = getBias() / 2F;
            return module.bias(bias).clamp(min, max);
        }
    }

    @Serializable
    public static class BiomeShape {

        @Range(min = 50, max = 1000)
        @Comment("Controls the size of individual biomes")
        public int biomeSize = 250;

        @Range(min = 1, max = 500)
        @Comment("Controls the scale of shape distortion for biomes")
        public int biomeWarpScale = 150;

        @Range(min = 1, max = 500)
        @Comment("Controls the strength of shape distortion for biomes")
        public int biomeWarpStrength = 80;
    }

    @Serializable
    public static class BiomeNoise {

        @Comment("The noise type")
        public Source type = Source.SIMPLEX;

        @Range(min = 1, max = 500)
        @Comment("Controls the scale of the noise")
        public int scale = 24;

        @Range(min = 1, max = 5)
        @Comment("Controls the number of noise octaves")
        public int octaves = 2;

        @Range(min = 0F, max = 5.5F)
        @Comment("Controls the gain subsequent noise octaves")
        public float gain = 0.5F;

        @Range(min = 0F, max = 10.5F)
        @Comment("Controls the lacunarity of subsequent noise octaves")
        public float lacunarity = 2.65F;

        @Range(min = 1, max = 500)
        @Comment("Controls the strength of the noise")
        public int strength = 14;

        public Module build(int seed) {
            return Source.build(seed, scale, octaves).gain(gain).lacunarity(lacunarity).build(type).bias(-0.5);
        }
    }
}
