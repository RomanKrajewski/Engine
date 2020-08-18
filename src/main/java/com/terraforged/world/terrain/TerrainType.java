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

public enum TerrainType implements ITerrain {
    NONE,
    DEEP_OCEAN {
        @Override
        public boolean isDeepOcean() {
            return true;
        }

        @Override
        public boolean overridesRiver() {
            return true;
        }

        @Override
        public boolean isSubmerged() {
            return true;
        }
    },
    SHALLOW_OCEAN {
        @Override
        public boolean isShallowOcean() {
            return true;
        }

        @Override
        public boolean isSubmerged() {
            return true;
        }

        @Override
        public boolean overridesRiver() {
            return true;
        }
    },
    COAST {
        @Override
        public boolean isCoast() {
            return true;
        }

        @Override
        public boolean isOverground() {
            return true;
        }

        @Override
        public boolean overridesRiver() {
            return true;
        }
    },
    BEACH {
        @Override
        public boolean isCoast() {
            return true;
        }

        @Override
        public boolean isOverground() {
            return true;
        }

        @Override
        public boolean overridesRiver() {
            return true;
        }
    },
    RIVER {
        @Override
        public boolean isRiver() {
            return true;
        }

        @Override
        public boolean isSubmerged() {
            return true;
        }
    },
    LAKE {
        @Override
        public boolean isLake() {
            return true;
        }

        @Override
        public boolean isSubmerged() {
            return true;
        }
    },
    WETLAND {
        @Override
        public boolean isWetland() {
            return true;
        }

        @Override
        public boolean isOverground() {
            return true;
        }
    },
    FLATLAND {
        @Override
        public boolean isFlat() {
            return true;
        }

        @Override
        public boolean isOverground() {
            return true;
        }
    },
    LOWLAND {
        @Override
        public boolean isOverground() {
            return true;
        }
    },
    HIGHLAND {
        @Override
        public boolean isOverground() {
            return true;
        }
    },
    ;



    public TerrainType getDominant(TerrainType other) {
        if (ordinal() > other.ordinal()) {
            return this;
        }
        return other;
    }
}
