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
