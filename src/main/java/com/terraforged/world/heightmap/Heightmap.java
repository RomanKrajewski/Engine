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

package com.terraforged.world.heightmap;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.module.Blender;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.settings.TerrainSettings;
import com.terraforged.core.settings.WorldSettings;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.func.EdgeFunc;
import com.terraforged.n2d.func.Interpolation;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.climate.Climate;
import com.terraforged.world.continent.Continent;
import com.terraforged.world.continent.ContinentLerper2;
import com.terraforged.world.continent.ContinentLerper3;
import com.terraforged.world.rivermap.RiverCache;
import com.terraforged.world.terrain.Terrain;
import com.terraforged.world.terrain.Terrains;
import com.terraforged.world.terrain.populator.TerrainPopulator;
import com.terraforged.world.terrain.provider.TerrainProvider;
import com.terraforged.world.terrain.region.RegionLerper;
import com.terraforged.world.terrain.region.RegionModule;
import com.terraforged.world.terrain.region.RegionSelector;

public class Heightmap implements Populator {

    public static final int MOUNTAIN_SCALE = 1000;
    public static final float DEEP_OCEAN_VALUE = 0.075F;
    public static final float SHALLOW_OCEAN_VALUE = 0.225F;
    public static final float BEACH_VALUE = 0.300F;
    public static final float COAST_VALUE = 0.350F;
    public static final float INLAND_VALUE = 0.5F;

    protected final Terrains terrain;
    private final TransitionPoints transitionPoints;

    protected final Continent continentGenerator;
    protected final Populator regionModule;

    private final Climate climate;
    private final Populator root;
    private final RiverCache riverMap;
    private final TerrainProvider terrainProvider;

    public Heightmap(GeneratorContext context) {
        context = context.copy();

        this.terrain = context.terrain;
        Settings settings = context.settings;

        WorldSettings world = context.settings.world;
        transitionPoints = new TransitionPoints(world.transitionPoints);

        Seed regionSeed = context.seed.nextSeed();
        Seed regionWarp = context.seed.nextSeed();

        int regionWarpScale = 400;
        int regionWarpStrength = 200;
        RegionConfig regionConfig = new RegionConfig(
                regionSeed.get(),
                context.settings.terrain.general.terrainRegionSize,
                Source.simplex(regionWarp.next(), regionWarpScale, 1),
                Source.simplex(regionWarp.next(), regionWarpScale, 1),
                regionWarpStrength
        );

        regionModule = new RegionModule(regionConfig);

        // controls where mountain chains form in the world
        Module mountainShapeBase = Source.cellEdge(context.seed.next(), MOUNTAIN_SCALE, EdgeFunc.DISTANCE_2_ADD)
                .warp(context.seed.next(), MOUNTAIN_SCALE / 3, 2, MOUNTAIN_SCALE / 4D);

        // sharpens the transition to create steeper mountains
        Module mountainShape = mountainShapeBase
                .curve(Interpolation.CURVE3)
                .clamp(0, 0.9)
                .map(0, 1);

        terrainProvider = context.terrainFactory.create(context, regionConfig, this);

        // the voronoi controlled terrain regions
        Populator terrainRegions = new RegionSelector(terrainProvider.getPopulators());
        // the terrain type at region edges
        Populator terrainRegionBorders = TerrainPopulator.of(
                context.terrain.steppe,
                terrainProvider.getLandforms().getLandBase(),
                terrainProvider.getLandforms().plains(context.seed),
                settings.terrain.steppe
        );

        // transitions between the unique terrain regions and the common border terrain
        Populator terrain = new RegionLerper(terrainRegionBorders, terrainRegions);

        // mountain populator
        Populator mountains = register(
                context.terrain.mountainChain,
                terrainProvider.getLandforms().getLandBase(),
                terrainProvider.getLandforms().mountains(context.seed),
                settings.terrain.mountains
        );

        // controls what's ocean and what's land
        continentGenerator = world.continent.continentMode.create(context.seed, world);
        climate = new Climate(continentGenerator, context);

        // blends between normal terrain and mountain chains
        Populator land = new Blender(
                mountainShape,
                terrain,
                mountains,
                0.3F,
                0.8F,
                0.575F
        );

        // uses the continent noise to blend between deep ocean, to ocean, to coast
        ContinentLerper3 oceans = new ContinentLerper3(
                climate,
                register(context.terrain.deepOcean, terrainProvider.getLandforms().deepOcean(context.seed.next())),
                register(context.terrain.ocean, Source.constant(context.levels.water(-7))),
                register(context.terrain.coast, Source.constant(context.levels.water)),
                transitionPoints.deepOcean, // below == deep, above == transition to shallow
                transitionPoints.shallowOcean,  // below == transition to deep, above == transition to coast
                transitionPoints.beach   // below == transition to shallow, above == beach
        );

        // blends between the ocean/coast terrain and land terrains
        root = new ContinentLerper2(
                oceans,
                land,
                transitionPoints.shallowOcean, // below == pure ocean
                transitionPoints.inland, // above == pure land
                transitionPoints.coast, // split point
                transitionPoints.coast - 0.05F
        );

        riverMap = new RiverCache(this, context);
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        applyBase(cell, x, z);
        applyRivers(cell, x, z);
        applyClimate(cell, x, z);
    }

    public void tag(Cell cell, float x, float z) {
        try (Resource<Cell> resource = Cell.pooled()) {
            continentGenerator.apply(resource.get(), x, z);
            regionModule.apply(resource.get(), x, z);
            root.apply(resource.get(), x, z);
            cell.terrain = resource.get().terrain;
        }
    }

    public void applyBase(Cell cell, float x, float z) {
        // initial type
        cell.terrain = terrain.steppe;
        // basic shapes
        continentGenerator.apply(cell, x, z);
        regionModule.apply(cell, x, z);
        // apply actual heightmap
        root.apply(cell, x, z);
    }

    public void applyRivers(Cell cell, float x, float z) {
        riverMap.getRivers(cell).apply(cell, x, z);
    }

    public void applyClimate(Cell cell, float x, float z) {
        // apply climate data
        climate.apply(cell, x, z);
    }

    public Climate getClimate() {
        return climate;
    }

    public Continent getContinent() {
        return continentGenerator;
    }

    public RiverCache getRivers() {
        return riverMap;
    }

    public TransitionPoints getTransitionPoints() {
        return transitionPoints;
    }

    public Populator getPopulator(Terrain terrain, int id) {
        return terrainProvider.getPopulator(terrain, id);
    }

    private TerrainPopulator register(Terrain terrain, Module variance) {
        TerrainPopulator populator = TerrainPopulator.of(terrain, variance);
        terrainProvider.registerMixable(populator);
        return populator;
    }

    private TerrainPopulator register(Terrain terrain, Module base, Module variance, TerrainSettings.Terrain settings) {
        TerrainPopulator populator = TerrainPopulator.of(terrain, base, variance, settings);
        terrainProvider.registerMixable(populator);
        return populator;
    }
}
