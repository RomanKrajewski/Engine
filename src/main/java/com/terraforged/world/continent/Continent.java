package com.terraforged.world.continent;

import com.terraforged.core.cell.Populator;
import com.terraforged.n2d.func.DistanceFunc;

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
