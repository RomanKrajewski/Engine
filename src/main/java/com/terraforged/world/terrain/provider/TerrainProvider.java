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

package com.terraforged.world.terrain.provider;

import com.terraforged.core.cell.Populator;
import com.terraforged.core.settings.TerrainSettings;
import com.terraforged.world.terrain.LandForms;
import com.terraforged.world.terrain.Terrain;
import com.terraforged.world.terrain.populator.TerrainPopulator;
import com.terraforged.n2d.Module;

import java.util.List;

/**
 * Provides the heightmap generator with terrain specific noise generation modules (TerrainPopulators)
 */
public interface TerrainProvider {

    LandForms getLandforms();

    /**
     * Returns the resulting set of TerrainPopulators
     */
    List<Populator> getPopulators();

    /**
     * Returns the number of Populator variants for the given terrain type
     */
    int getVariantCount(Terrain terrain);

    /**
     * Returns a populator for the provided Terrain, or the default if not registered
     */
    default Populator getPopulator(Terrain terrain) {
        return getPopulator(terrain, 0);
    }

    /**
     * Returns a populator for the provided Terrain, or the default if not registered
     */
    Populator getPopulator(Terrain terrain, int variant);

    /**
     * Add a TerrainPopulator to world generation.
     *
     * 'Mixable' TerrainPopulators are used to create additional terrain types, created by blending two
     * different mixable TerrainPopulators together (this is in addition to the unmixed version of the populator)
     */
    void registerMixable(TerrainPopulator populator);

    /**
     * Add a TerrainPopulator to world generation
     *
     * 'UnMixable' TerrainPopulators are NOT blended together to create additional terrain types
     */
    void registerUnMixable(TerrainPopulator populator);

    /**
     * Add a TerrainPopulator to world generation.
     *
     * 'Mixable' TerrainPopulators are used to create additional terrain types, created by blending two
     * different mixable TerrainPopulators together (this is in addition to the unmixed version of the populator)
     */
    default void registerMixable(Terrain type, Module base, Module variance, TerrainSettings.Terrain settings) {
        registerMixable(TerrainPopulator.of(type, base, variance, settings));
    }

    default void registerUnMixable(Terrain type, Module base, Module variance, TerrainSettings.Terrain settings) {
        registerUnMixable(TerrainPopulator.of(type, base, variance, settings));
    }

}
