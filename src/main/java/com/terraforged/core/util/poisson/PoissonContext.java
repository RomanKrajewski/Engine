package com.terraforged.core.util.poisson;

import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;

import java.util.Random;

public class PoissonContext {

    public int offsetX;
    public int offsetZ;
    public int startX;
    public int startZ;
    public int endX;
    public int endZ;
    public Module density = Source.ONE;

    public final int seed;
    public final Random random;

    public PoissonContext(long seed, Random random) {
        this.seed = (int) seed;
        this.random = random;
    }
}
