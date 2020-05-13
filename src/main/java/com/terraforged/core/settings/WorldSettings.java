package com.terraforged.core.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;
import com.terraforged.world.continent.WorldType;
import me.dags.noise.func.DistanceFunc;

@Serializable
public class WorldSettings {

    public transient long seed = 0L;

    @Comment("Controls the continent generator type")
    public WorldType worldType = WorldType.NORMAL;

    public Continent continent = new Continent();

    public Levels levels = new Levels();

    @Serializable
    public static class Continent {

        @Comment("Controls how continent shapes calculated")
        public DistanceFunc continentShape = DistanceFunc.NATURAL;

        @Range(min = 0F, max = 1F)
        @Comment("Controls the amount of ocean between continents")
        public float oceanScale = 0.45F;

        @Range(min = 100, max = 10000)
        @Comment("Controls the size of continents")
        public int continentScale = 3000;
    }

    @Serializable
    public static class Levels {

        @Range(min = 0, max = 256)
        @Comment("Controls the world height")
        public int worldHeight = 256;

        @Range(min = 0, max = 255)
        @Comment("Controls the sea level")
        public int seaLevel = 63;
    }
}
