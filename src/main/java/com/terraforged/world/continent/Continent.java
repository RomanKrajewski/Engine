package com.terraforged.world.continent;

import com.terraforged.core.cell.Populator;
import me.dags.noise.func.DistanceFunc;

public interface Continent extends Populator {

    float getEdgeNoise(float x, float y);

    void getNearestCenter(float x, float z, MutableVeci pos);

    default DistanceFunc getDistFunc() {
        return DistanceFunc.EUCLIDEAN;
    }

    default float getDistanceToEdge(int cx, int cz, float dx, float dy, MutableVeci pos) {
        return 1F;
    }

    default float getDistanceToOcean(int cx, int cz, float dx, float dy, MutableVeci pos) {
        return 1F;
    }
}
