package com.terraforged.core.util.erosion;

import com.terraforged.n2d.Module;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.n2d.util.Vec2f;

import java.util.Arrays;

public class PseudoErosion {

    private final int seed;
    private final float strength;
    private final float gridSize;
    private final Mode blendMode;

    public PseudoErosion(int seed, float strength, float gridSize, Mode blendMode) {
        this.strength = strength;
        this.gridSize = gridSize;
        this.blendMode = blendMode;
        this.seed = seed;
    }

    public float getValue(float x, float y, Module source) {
        return getValue(x, y, source, createCache());
    }

    public float getValue(float x, float y, Module source, float[] cache) {
        float value = source.getValue(x, y);
        float erosion = getErosionValue(x, y, source, cache);
        return NoiseUtil.lerp(erosion, value, blendMode.blend(value, erosion, strength));
    }

    public float getErosionValue(float x, float y, Module source) {
        return getErosionValue(x, y, source, createCache());
    }

    public float getErosionValue(float x, float y, Module source, float[] cache) {
        cache = PseudoErosion.initCache(cache);

        int pix = NoiseUtil.round(x / gridSize);
        int piy = NoiseUtil.round(y / gridSize);

        Vec2f vec;

        int pax, pay, pbx, pby;
        float ax, ay, bx, by, candidateX, candidateY, height, height2, minHeight2 = 10000, lowestNeighbour;
        for (int dy1 = -1; dy1 <= 1; dy1++) {
            for (int dx1 = -1; dx1 <= 1; dx1++) {
                // ivec2 pa = pi + ivec2(i,j);
                pax = pix + dx1;
                pay = piy + dy1;
                vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, pax, pay) & 255];

                // vec2  p1 = (vec2(pa) + hash2(pa)) * gridSize;
                ax = (pax + vec.x) * gridSize;
                ay = (pay + vec.y) * gridSize;

                bx = ax;
                by = ay;

                lowestNeighbour = 100000;
                for (int dy2 = -1; dy2 <= 1; dy2++) {
                    for (int dx2 = -1; dx2 <= 1; dx2++) {
                        // ivec2 pb = pa + ivec2(m,n);
                        pbx = pax + dx2;
                        pby = pay + dy2;
                        vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, pbx, pby) & 255];

                        // vec2 candidate = (vec2(pb) + hash2(pb)) * gridSize;
                        candidateX = (pbx + vec.x) * gridSize;
                        candidateY = (pby + vec.y) * gridSize;

                        height = getNoiseValue(dx1 + dx2 ,dy1 + dy2, candidateX, candidateY, source, cache);
                        if (height < lowestNeighbour) {
                            lowestNeighbour = height;
                            bx = candidateX;
                            by = candidateY;
                        }
                    }
                }

                height2 = sd(x, y, ax, ay, bx, by);
                if (height2 < minHeight2) {
                    minHeight2 = height2;
                }
            }
        }
        return NoiseUtil.clamp(sqrt(minHeight2) / gridSize, 0, 1);
    }

    private static float getNoiseValue(int dx, int dy, float px, float py, Module module, float[] cache) {
        int index = ((dy + 2) * 5) + (dx + 2);
        float value = cache[index];
        if (value == -1) {
            value = module.getValue(px, py);
            cache[index] = value;
        }
        return value;
    }

    private static float sd(float px, float py, float ax, float ay, float bx, float by) {
        // vec2 pa = p-a, ba = b-a;
        float padx = px - ax;
        float pady = py - ay;

        float badx = bx - ax;
        float bady = by - ay;

        // float h = clamp( dot(pa,ba)/dot(ba,ba), 0.0, 1.0 );
        float dotpaba = dot(padx, pady, badx, bady);
        float dotbaba = dot(badx, bady, badx, bady);
        float h = NoiseUtil.clamp(dotpaba / dotbaba, 0, 1);

        // return length( pa - ba*h ); ???????
        return len2(padx, pady, badx * h, bady * h);
    }

    private static float dot(float x0, float y0, float x1, float y1) {
        return x0 * x1 + y0 * y1;
    }

    private static float len2(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (dx * dx + dy * dy);
    }

    private static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public enum Mode {
        STRENGTH {
            @Override
            public float blend(float value, float erosion, float strength) {
                return 1 - strength;
            }
        },
        HEIGHT {
            @Override
            public float blend(float value, float erosion, float strength) {
                return 1 - (strength * value);
            }
        },
        EROSION {
            @Override
            public float blend(float value, float erosion, float strength) {
                return 1 - (strength * erosion);
            }
        },
        ;

        public abstract float blend(float value, float erosion, float strength);
    }

    private static float[] initCache(float[] cache) {
        if (cache.length < 25) {
            cache = createCache();
        }
        Arrays.fill(cache, -1);
        return cache;
    }

    public static float[] createCache() {
        return new float[25];
    }

    public static Module wrap(int seed, float strength, float gridSize, Module source) {
        return new PseudoErosionModule(seed, strength, gridSize, Mode.HEIGHT, source);
    }

    public static Module wrap(int seed, float strength, float gridSize, Mode mode, Module source) {
        return new PseudoErosionModule(seed, strength, gridSize, mode, source);
    }
}
