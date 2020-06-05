package com.terraforged.world.geology;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.pool.ObjectPool;

public class DepthBuffer {

    private static final ObjectPool<DepthBuffer> pool = new ObjectPool<>(5, DepthBuffer::new);

    private float sum;
    private float[] buffer;

    public void init(int size) {
        sum = 0F;
        if (buffer == null || buffer.length < size) {
            buffer = new float[size];
        }
    }

    public float getSum() {
        return sum;
    }

    public float get(int index) {
        return buffer[index];
    }

    public float getDepth(int index) {
        return buffer[index] / sum;
    }

    public void set(int index, float value) {
        sum += value;
        buffer[index] = value;
    }

    public static Resource<DepthBuffer> get() {
        return pool.get();
    }
}
