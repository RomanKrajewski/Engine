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

package com.terraforged.core.concurrent.thread;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;
import com.terraforged.core.concurrent.batch.ForkJoinBatcher;
import com.terraforged.core.concurrent.pool.ObjectPool;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class ForkJoinThreadPool implements ThreadPool {

    private final int size;
    private final boolean keepAlive;
    private final ForkJoinPool executor;
    private final ObjectPool<Batcher> batchers;

    public ForkJoinThreadPool(int size, boolean keepAlive) {
        this.size = size;
        this.keepAlive = keepAlive;
        this.executor = new ForkJoinPool(size, new WorkerFactory.ForkJoin("TF-Fork"), null, true);
        this.batchers = new ObjectPool<>(10, () -> new ForkJoinBatcher(executor));
    }

    @Override
    public boolean supportsBatching() {
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return executor.submit(runnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }

    @Override
    public boolean keepAlive() {
        return keepAlive;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        ThreadPools.shutdown(this);
    }

    @Override
    public Resource<Batcher> batcher() {
        return batchers.get();
    }
}
