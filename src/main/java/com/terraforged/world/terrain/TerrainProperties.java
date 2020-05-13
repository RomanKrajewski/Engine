package com.terraforged.world.terrain;

public interface TerrainProperties {

    default boolean isRiver() {
        return false;
    }

    default boolean isShallowOcean() {
        return false;
    }

    default boolean isDeepOcean() {
        return false;
    }

    default boolean isLake() {
        return false;
    }

    default boolean isWetland() {
        return false;
    }
}
