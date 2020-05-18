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

package com.terraforged.world.terrain;

import com.terraforged.core.settings.Settings;
import com.terraforged.world.heightmap.Levels;
import me.dags.noise.util.NoiseUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Terrain implements TerrainType {

    private static final Map<String, Terrain> register = Collections.synchronizedMap(new HashMap<>());

    public static final int ID_START = 10;
    public static final Terrain NONE = new Terrain("none", -1);

    private final String name;
    private final float weight;

    public Terrain(String name) {
        this(name, 1F);
    }

    public Terrain(String name, double weight) {
        this.name = name;
        this.weight = (float) weight;
        Terrain.register.put(name, this);
    }

    public float getMax(float noise) {
        return 1F;
    }

    public float getWeight() {
        return weight;
    }

    public float getHue() {
        return NoiseUtil.valCoord2D(name.hashCode(), 0, 0);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static Terrain ocean(Settings settings) {
        return new Terrain("ocean", 0) {

            private final float max = new Levels(settings.world).water;

            @Override
            public float getMax(float noise) {
                return max;
            }

            @Override
            public boolean isShallowOcean() {
                return true;
            }
        };
    }

    public static Terrain deepOcean(Settings settings) {
        return new Terrain("deep_ocean", 1) {

            private final float max = new Levels(settings.world).water / 2F;

            @Override
            public float getMax(float noise) {
                return max;
            }

            @Override
            public boolean isDeepOcean() {
                return true;
            }
        };
    }

    public static Terrain coast(Settings settings) {
        return new Terrain("coast", 2) {

            private final float max = new Levels(settings.world).ground(1);

            @Override
            public float getMax(float noise) {
                return max + (noise * (1 / 255F));
            }
        };
    }

    public static Terrain beach(Settings settings) {
        return new Terrain("beach", 2) {

            private final float max = new Levels(settings.world).ground(1);

            @Override
            public float getMax(float noise) {
                return max + (noise * (1 / 255F));
            }
        };
    }

    public static Terrain lake(Settings settings) {
        return new Terrain("lake", 3) {
            @Override
            public boolean isLake() {
                return true;
            }
        };
    }

    public static Terrain river(Settings settings) {
        return new Terrain("river", 3) {
            @Override
            public boolean isRiver() {
                return true;
            }
        };
    }

    public static Terrain riverBank(Settings settings) {
        return new Terrain("river_banks", 4) {
            @Override
            public boolean isRiver() {
                return true;
            }
        };
    }

    public static Terrain wetlands(Settings settings) {
        return new Terrain("wetlands", 5) {
            @Override
            public boolean isWetland() {
                return true;
            }
        };
    }

    public static Terrain desert(Settings settings) {
        return new Terrain("desert", 5) {

        };
    }

    public static Terrain steppe(Settings settings) {
        return new Terrain("steppe", settings.terrain.steppe.weight) {
            @Override
            public boolean isFlat() {
                return true;
            }
        };
    }

    public static Terrain plains(Settings settings) {
        return new Terrain("plains", settings.terrain.plains.weight) {
            @Override
            public boolean isFlat() {
                return true;
            }
        };
    }

    public static Terrain plateau(Settings settings) {
        return new Terrain("plateau", settings.terrain.plateau.weight);
    }

    public static Terrain badlands(Settings settings) {
        return new Terrain("badlands", settings.terrain.badlands.weight) {
            @Override
            public boolean isFlat() {
                return true;
            }
        };
    }

    public static Terrain hills(Settings settings) {
        return new Terrain("hills", settings.terrain.hills.weight);
    }

    public static Terrain dales(Settings settings) {
        return new Terrain("dales", settings.terrain.dales.weight);
    }

    public static Terrain torridonian(Settings settings) {
        return new Terrain("torridonian_fells", settings.terrain.torridonian.weight);
    }

    public static Terrain mountains(Settings settings) {
        return new Terrain("mountains", settings.terrain.mountains.weight);
    }

    public static Terrain volcano(Settings settings) {
        return new Terrain("volcano", settings.terrain.volcano.weight);
    }

    public static Terrain volcanoPipe(Settings settings) {
        return new Terrain("volcano_pipe", settings.terrain.volcano.weight);
    }

    public static Optional<Terrain> get(String name) {
        return Optional.ofNullable(register.get(name));
    }

    public static List<Terrain> getRegistered() {
        return new ArrayList<>(register.values());
    }
}
