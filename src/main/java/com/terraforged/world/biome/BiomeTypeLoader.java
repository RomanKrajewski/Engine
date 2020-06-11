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

package com.terraforged.world.biome;

import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.n2d.util.Vec2f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BiomeTypeLoader {

    private static BiomeTypeLoader instance;

    private final BiomeType[][] map = new BiomeType[BiomeType.RESOLUTION][BiomeType.RESOLUTION];

    public BiomeTypeLoader() {
        generateTypeMap();
    }

    public BiomeType[][] getTypeMap() {
        return map;
    }

    public Vec2f[] getRanges(BiomeType type) {
        float minTemp = 1F;
        float maxTemp = 0F;
        float minMoist = 1F;
        float maxMoist = 0F;
        for (int moist = 0; moist < map.length; moist++) {
            BiomeType[] row = map[moist];
            for (int temp = 0; temp < row.length; temp++) {
                BiomeType t = row[temp];
                if (t == type) {
                    float temperature = temp / (float) (row.length - 1);
                    float moisture = moist / (float) (map.length - 1);
                    minTemp = Math.min(minTemp, temperature);
                    maxTemp = Math.max(maxTemp, temperature);
                    minMoist = Math.min(minMoist, moisture);
                    maxMoist = Math.max(maxMoist, moisture);
                }
            }
        }
        return new Vec2f[]{new Vec2f(minTemp, maxTemp), new Vec2f(minMoist, maxMoist)};
    }

    private BiomeType getType(int x, int y) {
        return map[y][x];
    }

    private void generateTypeMap() {
        try {
            BufferedImage image = ImageIO.read(BiomeType.class.getResourceAsStream("/biomes.png"));
            float xf = image.getWidth() / (float) BiomeType.RESOLUTION;
            float yf = image.getHeight() / (float) BiomeType.RESOLUTION;
            for (int y = 0; y < BiomeType.RESOLUTION; y++) {
                for (int x = 0; x < BiomeType.RESOLUTION; x++) {
                    if (BiomeType.MAX - y > x) {
                        map[BiomeType.MAX - y][x] = BiomeType.ALPINE;
                        continue;
                    }
                    int ix = NoiseUtil.round(x * xf);
                    int iy = NoiseUtil.round(y * yf);
                    int argb = image.getRGB(ix, iy);
                    Color color = fromARGB(argb);
                    map[BiomeType.MAX - y][x] = forColor(color);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BiomeType forColor(Color color) {
        BiomeType type = null;
        int closest = Integer.MAX_VALUE;
        for (BiomeType t : BiomeType.values()) {
            int distance2 = getDistance2(color, t.getLookup());
            if (distance2 < closest) {
                closest = distance2;
                type = t;
            }
        }
        if (type == null) {
            return BiomeType.GRASSLAND;
        }
        return type;
    }

    private static int getDistance2(Color a, Color b) {
        int dr = a.getRed() - b.getRed();
        int dg = a.getGreen() - b.getGreen();
        int db = a.getBlue() - b.getBlue();
        return dr * dr + dg * dg + db * db;
    }

    private static Color fromARGB(int argb) {
        int b = (argb) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        return new Color(r, g, b);
    }

    private static int dist2(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    public static BiomeTypeLoader getInstance() {
        if (instance == null) {
            instance = new BiomeTypeLoader();
        }
        return instance;
    }
}
