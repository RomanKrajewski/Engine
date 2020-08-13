/*
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

public interface ITerrain {

    default float erosionModifier() {
        return 1F;
    }

    default boolean isFlat() {
        return false;
    }

    default boolean isRiver() {
        return false;
    }

    default boolean isShallowOcean() {
        return false;
    }

    default boolean isDeepOcean() {
        return false;
    }

    default boolean isCoast() {
        return false;
    }

    default boolean isSubmerged() {
        return isDeepOcean() || isShallowOcean() || isRiver() || isLake();
    }

    default boolean isOverground() {
        return false;
    }

    default boolean overridesRiver() {
        return isDeepOcean() || isShallowOcean() || isCoast();
    }

    default boolean isLake() {
        return false;
    }

    default boolean isWetland() {
        return false;
    }

    default boolean isMountain() {
        return false;
    }

    interface Delegate extends ITerrain {

        TerrainType getType();

        @Override
        default float erosionModifier() {
            return getType().erosionModifier();
        }

        @Override
        default boolean isFlat() {
            return getType().isFlat();
        }

        @Override
        default boolean isRiver() {
            return getType().isRiver();
        }

        @Override
        default boolean isShallowOcean() {
            return getType().isShallowOcean();
        }

        @Override
        default boolean isDeepOcean() {
            return getType().isDeepOcean();
        }

        @Override
        default boolean isCoast() {
            return getType().isCoast();
        }

        @Override
        default boolean overridesRiver() {
            return getType().overridesRiver();
        }

        @Override
        default boolean isLake() {
            return getType().isLake();
        }

        @Override
        default boolean isWetland() {
            return getType().isWetland();
        }

        @Override
        default boolean isOverground() {
            return getType().isOverground();
        }

        @Override
        default boolean isSubmerged() {
            return getType().isSubmerged();
        }

        @Override
        default boolean isMountain() {
            return getType().isMountain();
        }
    }
}
