package com.terraforged.world.terrain;

public class MixedTerarin extends Terrain {

    private final boolean flat;
    private final float erosion;

    public MixedTerarin(Terrain a, Terrain b) {
        super(a.getName() + "-" + b.getName(), Math.min(a.getWeight(), b.getWeight()), a.getType().getDominant(b.getType()));
        flat = a.isFlat() && b.isFlat();
        erosion = Math.min(a.erosionModifier(), b.erosionModifier());
    }

    @Override
    public float erosionModifier() {
        return erosion;
    }

    @Override
    public boolean isFlat() {
        return flat;
    }
}
