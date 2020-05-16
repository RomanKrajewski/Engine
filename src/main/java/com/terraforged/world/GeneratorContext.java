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

package com.terraforged.world;

import com.terraforged.core.Seed;
import com.terraforged.core.concurrent.pool.ThreadPools;
import com.terraforged.core.region.gen.RegionCache;
import com.terraforged.core.region.gen.RegionGenerator;
import com.terraforged.core.settings.Settings;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.TerrainTypes;
import com.terraforged.world.terrain.provider.StandardTerrainProvider;
import com.terraforged.world.terrain.provider.TerrainProviderFactory;

import java.util.function.Function;

public class GeneratorContext {

    public final Seed seed;
    public final Levels levels;
    public final TerrainTypes terrain;
    public final Settings settings;
    public final RegionCache cache;
    public final WorldGeneratorFactory factory;
    public final TerrainProviderFactory terrainFactory;

    public GeneratorContext(TerrainTypes terrain, Settings settings) {
        this(terrain, settings, StandardTerrainProvider::new, GeneratorContext::createCache);
    }

    public <T extends Settings> GeneratorContext(TerrainTypes terrain, T settings, TerrainProviderFactory terrainFactory, Function<WorldGeneratorFactory, RegionCache> cache) {
        this.terrain = terrain;
        this.settings = settings;
        this.seed = new Seed(settings.world.seed);
        this.levels = new Levels(settings.world);
        this.terrainFactory = terrainFactory;
        this.factory = createFactory(this);
        this.cache = cache.apply(factory);
    }

    private GeneratorContext(GeneratorContext src) {
        seed = new Seed(src.seed.get());
        cache = src.cache;
        levels = src.levels;
        factory = src.factory;
        terrain = src.terrain;
        settings = src.settings;
        terrainFactory = src.terrainFactory;
    }

    protected WorldGeneratorFactory createFactory(GeneratorContext context) {
        return new WorldGeneratorFactory(context);
    }

    public GeneratorContext copy() {
        return new GeneratorContext(this);
    }

    public static GeneratorContext createNoCache(TerrainTypes terrain, Settings settings) {
        return new GeneratorContext(terrain, settings, StandardTerrainProvider::new, s -> null);
    }

    protected static <T extends Settings> RegionCache createCache(WorldGeneratorFactory factory) {
        return RegionGenerator.builder()
                .factory(factory)
                .size(3, 2)
                .pool(ThreadPools.getPool())
                .build()
                .toCache(false);
    }
}
