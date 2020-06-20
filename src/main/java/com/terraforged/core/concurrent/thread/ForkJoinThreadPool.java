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
    private final ForkJoinPool executor;
    private final ObjectPool<Batcher> batchers;

    public ForkJoinThreadPool(int size) {
        this.size = size;
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
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public Resource<Batcher> batcher() {
        return batchers.get();
    }
}
