package com.terraforged.core.render;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.heightmap.Levels;
import me.dags.noise.util.NoiseUtil;

import java.awt.*;

public enum RenderMode {
    BIOME_TYPE {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            Color c = cell.biomeType.getColor();
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            color(buffer, hsb[0] * 100, hsb[1] * 100, hsb[2] * 100, height, 0.5F, context.levels);
        }
    },
    ELEVATION {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float temp = cell.temperature;
            float moist = Math.min(temp, cell.moisture);
            float hue = 35 - (temp * (1 - moist)) * 25;
            color(buffer, hue, 70, 80, height, 0.3F, context.levels);
        }
    },
    TEMPERATURE {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = hue(1 - cell.temperature, 8, 70);
            color(buffer, hue, 70, 80, height, 0.35F, context.levels);
        }
    },
    MOISTURE {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = hue(cell.moisture, 64, 70);
            color(buffer, hue, 70, 80, height, 0.35F, context.levels);
        }
    },
    BIOME {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = cell.biome * 70;
            color(buffer, hue, 70, 80, height, 0.4F, context.levels);
        }
    },
    STEEPNESS {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = hue(1 - cell.steepness, 64, 70);
            color(buffer, hue, 70, 70, height, 0.4F, context.levels);
        }
    },
    TERRAIN_TYPE {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = 20 + (cell.terrain.getHue() * 80);
            if (cell.terrain == context.terrain.coast) {
                hue = 15;
            }
            if (cell.continentEdge < 0.01F) {
                hue = 70;
            }
            color(buffer, hue, 70, 70, height, 0.4F, context.levels);
        }
    },
    CONTINENT {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = cell.continent * 70;
            color(buffer, hue, 70, 70, height, 0.4F, context.levels);
        }
    },
    ;

    public abstract void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context);

    public void fillColor(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
        if (height <= context.levels.waterLevel) {
            float temp = cell.temperature;
            float tempDelta = temp > 0.5 ? temp - 0.5F : -(0.5F - temp);
            float tempAlpha = (tempDelta / 0.5F);
            float hueMod = 4 * tempAlpha;

            float depth = (context.levels.waterLevel - height) / (float) (90);
            float darkness = (1 - depth);
            float darknessMod = 0.5F + (darkness * 0.5F);

            buffer.color(60 - hueMod, 65, 90 * darknessMod);
        } else {
            fill(cell, height, buffer, context);
        }
    }

    private static float hue(float value, int steps, int max) {
        value = Math.round(value * (steps - 1));
        value /= (steps - 1);
        return value * max;
    }

    private static void color(RenderBuffer buffer, float hue, float saturation, float brightness, float height, float strength, Levels levels) {
        float value = NoiseUtil.clamp((height - levels.waterLevel) / (levels.worldHeight - levels.waterLevel), 0F, 1F);
        float shade = (1 - strength) + (value * strength);
        float sat = saturation * (1 - shade * 0.1F);
        float bri = brightness * shade;
        buffer.color(hue, sat, bri);
    }

    private static float brightness(float value, Cell cell, Levels levels, float strength) {
        if (cell.value <= levels.water) {
            return value;
        }
        float alpha = (cell.value - levels.water) / (1F - levels.water);
        alpha = (1 - strength) + (alpha * strength);
        return value * alpha;
    }
}
