package com.terraforged.world.terrain;

public interface TerrainType {

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

    default boolean overridesRiver() {
        return isDeepOcean() || isShallowOcean() || isCoast();
    }

    default boolean isLake() {
        return false;
    }

    default boolean isWetland() {
        return false;
    }
}
