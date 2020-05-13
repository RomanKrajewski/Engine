package com.terraforged.world.continent;

import com.terraforged.core.settings.WorldSettings;
import com.terraforged.core.Seed;
import com.terraforged.world.continent.generator.MultiContinentGenerator;
import com.terraforged.world.continent.generator.SingleContinentGenerator;

public enum WorldType {
    NORMAL {
        @Override
        public Continent create(Seed seed, WorldSettings settings) {
            return new MultiContinentGenerator(seed, settings);
        }
    },
    SINGLE_CONTINENT {
        @Override
        public Continent create(Seed seed, WorldSettings settings) {
            return new SingleContinentGenerator(seed, settings);
        }
    },
    ;

    public abstract Continent create(Seed seed, WorldSettings settings);
}
