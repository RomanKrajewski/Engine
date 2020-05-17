package com.terraforged.world.terrain;

public class MixedTerarin extends Terrain {

    private final boolean flat;

    public MixedTerarin(Terrain a, Terrain b) {
        super(a.getName() + "-" + b.getName(), Math.min(a.getWeight(), b.getWeight()));
        flat = a.isFlat() && b.isFlat();
    }

    @Override
    public boolean isFlat() {
        return flat;
    }
}
