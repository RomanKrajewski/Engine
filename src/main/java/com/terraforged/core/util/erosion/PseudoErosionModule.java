package com.terraforged.core.util.erosion;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.pool.ObjectPool;
import com.terraforged.n2d.Module;

public class PseudoErosionModule extends PseudoErosion implements Module {

    private static final ObjectPool<float[]> pool = new ObjectPool<>(10, PseudoErosion::createCache);

    private final Module source;

    public PseudoErosionModule(int seed, float strength, float gridSize, Mode blendMode, Module source) {
        super(seed, strength, gridSize, blendMode);
        this.source = source;
    }

    @Override
    public float getValue(float x, float y) {
        try (Resource<float[]> cache = pool.get()) {
            return getValue(x, y, source, cache.get());
        }
    }
}
