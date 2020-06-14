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
import com.terraforged.n2d.util.NoiseUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Terrain implements ITerrain.Delegate {

    private static final Map<String, Terrain> register = Collections.synchronizedMap(new HashMap<>());
    public static final Terrain NONE = new Terrain("none", -1, TerrainType.NONE);

    private final String name;
    private final float weight;
    private final TerrainType type;

    public Terrain(String name, TerrainType type) {
        this(name, 1F, type);
    }

    public Terrain(String name, double weight, TerrainType type) {
        this.name = name;
        this.weight = (float) weight;
        this.type = type;
        Terrain.register.put(name, this);
    }

    @Override
    public TerrainType getType() {
        return type;
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
        return new Terrain("ocean", 0, TerrainType.SHALLOW_OCEAN) {

            private final float max = new Levels(settings.world).water;

            @Override
            public float getMax(float noise) {
                return max;
            }
        };
    }

    public static Terrain deepOcean(Settings settings) {
        return new Terrain("deep_ocean", 1, TerrainType.DEEP_OCEAN) {

            private final float max = new Levels(settings.world).water / 2F;

            @Override
            public float getMax(float noise) {
                return max;
            }
        };
    }

    public static Terrain coast(Settings settings) {
        return new Terrain("coast", 2, TerrainType.COAST) {

            private final float max = new Levels(settings.world).ground(1);

            @Override
            public float getMax(float noise) {
                return max + (noise * (1 / 255F));
            }
        };
    }

    public static Terrain beach(Settings settings) {
        return new Terrain("beach", 2, TerrainType.COAST) {

            private final float max = new Levels(settings.world).ground(1);

            @Override
            public float getMax(float noise) {
                return max + (noise * (1 / 255F));
            }
        };
    }

    public static Terrain lake(Settings settings) {
        return new Terrain("lake", 3, TerrainType.LAKE);
    }

    public static Terrain river(Settings settings) {
        return new Terrain("river", 3, TerrainType.RIVER);
    }

    public static Terrain riverBank(Settings settings) {
        return new Terrain("river_banks", 4, TerrainType.RIVER);
    }

    public static Terrain wetlands(Settings settings) {
        return new Terrain("wetlands", 5, TerrainType.WETLAND);
    }

    public static Terrain desert(Settings settings) {
        return new Terrain("desert", 5, TerrainType.FLATLAND);
    }

    public static Terrain steppe(Settings settings) {
        return new Terrain("steppe", settings.terrain.steppe.weight, TerrainType.FLATLAND);
    }

    public static Terrain plains(Settings settings) {
        return new Terrain("plains", settings.terrain.plains.weight, TerrainType.FLATLAND);
    }

    public static Terrain badlands(Settings settings) {
        return new Terrain("badlands", settings.terrain.badlands.weight, TerrainType.FLATLAND) {
            @Override
            public float erosionModifier() {
                return 0.25F;
            }
        };
    }

    public static Terrain plateau(Settings settings) {
        return new Terrain("plateau", settings.terrain.plateau.weight, TerrainType.LOWLAND);
    }

    public static Terrain hills(Settings settings) {
        return new Terrain("hills", settings.terrain.hills.weight, TerrainType.LOWLAND);
    }

    public static Terrain dales(Settings settings) {
        return new Terrain("dales", settings.terrain.dales.weight, TerrainType.LOWLAND);
    }

    public static Terrain torridonian(Settings settings) {
        return new Terrain("torridonian", settings.terrain.torridonian.weight, TerrainType.HIGHLAND);
    }

    public static Terrain mountains(Settings settings) {
        return new Terrain("mountains", settings.terrain.mountains.weight, TerrainType.HIGHLAND);
    }

    public static Terrain volcano(Settings settings) {
        return new Terrain("volcano", settings.terrain.volcano.weight, TerrainType.HIGHLAND);
    }

    public static Terrain volcanoPipe(Settings settings) {
        return new Terrain("volcano_pipe", settings.terrain.volcano.weight, TerrainType.HIGHLAND);
    }

    public static Optional<Terrain> get(String name) {
        return Optional.ofNullable(register.get(name));
    }

    public static List<Terrain> getRegistered() {
        return new ArrayList<>(register.values());
    }
}
