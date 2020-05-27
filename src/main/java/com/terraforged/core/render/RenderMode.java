package com.terraforged.core.render;

import com.terraforged.core.cell.Cell;

import java.awt.*;

public enum RenderMode {
    BIOME_TYPE {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            Color c = cell.biomeType.getColor();
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            float bri = 90;
            buffer.color(hsb[0] * 100, hsb[1] * 100, hsb[2] * bri);
        }
    },
    ELEVATION {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hei = Math.min(1, Math.max(0, height - context.levels.waterLevel) / (255F - context.levels.waterLevel));
            float temp = cell.temperature;
            float moist = Math.min(temp, cell.moisture);

            float hue = 35 - (temp * (1 - moist)) * 25;
            float sat = 75 * (1 - hei);
            float bri = 50 + 40 * hei;
            buffer.color(hue, sat, bri);
        }
    },
    TEMPERATURE {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = hue(1 - cell.temperature, 8, 70);
            float sat = 70;
            float bri = 70;
            buffer.color(hue, sat, bri);
        }
    },
    MOISTURE {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = hue(cell.moisture, 64, 70);
            float sat = 70;
            float bri = 70;
            buffer.color(hue, sat, bri);
        }
    },
    BIOME {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = cell.biome * 70;
            float sat = 70;
            float bright = 50 + 50 * cell.riverMask;
            buffer.color(0, 0, (cell.biomeEdge) * 100);
        }
    },
    STEEPNESS {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = hue(1 - cell.steepness, 64, 70);
            float sat = 70;
            float bri = 70;
            buffer.color(hue, sat, bri);
        }
    },
    TERRAIN_TYPE {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = 20 + (cell.terrainType.getHue() * 80);
            if (cell.terrainType == context.terrain.coast) {
                hue = 15;
            }
            if (cell.continentEdge < 0.01F) {
                hue = 70;
            }
            float modifier = cell.mask(0.4F, 0.5F, 0F, 1F);
            float modAlpha = 0.1F;
            buffer.color(hue, 65, 70);
        }
    },
    CONTINENT {
        @Override
        public void fill(Cell cell, float height, RenderBuffer buffer, RenderSettings context) {
            float hue = cell.continent * 70;
            float sat = 70;
            float bri = 70;
            buffer.color(hue, sat, bri);
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
}
