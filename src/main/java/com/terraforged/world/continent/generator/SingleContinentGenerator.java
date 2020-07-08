package com.terraforged.world.continent.generator;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.WorldSettings;
import com.terraforged.n2d.util.Vec2i;
import com.terraforged.world.continent.MutableVeci;

public class SingleContinentGenerator extends ContinentGenerator {

    private final Vec2i center;

    public SingleContinentGenerator(Seed seed, WorldSettings settings) {
        super(seed, settings);
        MutableVeci pos = new MutableVeci();
        getNearestCenter(0, 0, pos);
        this.center = new Vec2i(pos.x, pos.z);
    }

    @Override
    public void apply(Cell cell, final float x, final float y) {
        super.apply(cell, x, y);

        // reset if outside of the world's only continent
        if (cell.continentX != center.x || cell.continentZ != center.y) {
            cell.continentIdentity = 0F;
            cell.continentEdge = 0F;
            cell.continentX = 0;
            cell.continentZ = 0;
        }
    }
}
