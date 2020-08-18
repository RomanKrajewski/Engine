/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
