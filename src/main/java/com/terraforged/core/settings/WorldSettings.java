package com.terraforged.core.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;
import com.terraforged.n2d.func.DistanceFunc;
import com.terraforged.world.continent.ContinentMode;
import com.terraforged.world.continent.SpawnType;
import com.terraforged.world.heightmap.Heightmap;

@Serializable
public class WorldSettings {

    public static final int DEFAULT_CONTINENT_SCALE = 3000;

    public transient long seed = 0L;

    public Continent continent = new Continent();

    public TransitionPoints transitionPoints = new TransitionPoints();

    public Properties properties = new Properties();

    @Serializable
    public static class Continent {

        @Comment("Controls the continent generator type")
        public ContinentMode continentMode = ContinentMode.MULTI;

        @Comment("Controls how continent shapes are calculated")
        public DistanceFunc continentShape = DistanceFunc.EUCLIDEAN;

        @Range(min = 0F, max = 1F)
        @Comment("Controls the amount of ocean between continents")
        public float oceanScale = 0.8F;

        @Range(min = 100, max = 10000)
        @Comment("Controls the size of continents")
        public int continentScale = DEFAULT_CONTINENT_SCALE;
    }

    @Serializable
    public static class TransitionPoints {

        @Range(min = 0F, max = 1F)
        @Comment({
                "The point at which deep oceans transition into shallow oceans.",
                "The value must be lower than the next transition point. A larger",
                "distance to the next transition point will produce a more gradual",
                "transition."
        })
        public float deepOcean = Heightmap.DEEP_OCEAN_VALUE;

        @Range(min = 0F, max = 1F)
        @Comment({
                "The point at which shallow oceans transition into beaches.",
                "The value must be lower than the next transition point. A larger",
                "distance to the next transition point will produce a more gradual",
                "transition."
        })
        public float shallowOcean = Heightmap.OCEAN_VALUE;

        @Range(min = 0F, max = 1F)
        @Comment({
                "The point at which beaches transition into coastal terrain.",
                "The value must be lower than the next transition point. A larger",
                "distance to the next transition point will produce a more gradual",
                "transition."
        })
        public float beach = Heightmap.BEACH_VALUE;

        @Range(min = 0F, max = 1F)
        @Comment({
                "The point at which coasts transition into normal inland terrain.",
                "The value must be lower than the next transition point. A larger",
                "distance to the next transition point will produce a more gradual",
                "transition."
        })
        public float coast = Heightmap.COAST_VALUE;

        @Range(min = 0F, max = 1F)
        @Comment("The point above which terrain is purely normal inland terrain.")
        public float inland = Heightmap.INLAND_VALUE;
    }

    @Serializable
    public static class Properties {

        @Comment("Set whether spawn should be close to x=0,z=0 or the centre of the nearest continent")
        public SpawnType spawnType = SpawnType.CONTINENT_CENTER;

        @Range(min = 0, max = 256)
        @Comment("Controls the world height")
        public int worldHeight = 256;

        @Range(min = 0, max = 255)
        @Comment("Controls the sea level")
        public int seaLevel = 63;
    }
}
