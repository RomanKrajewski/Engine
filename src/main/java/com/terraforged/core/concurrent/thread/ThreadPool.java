package com.terraforged.core.concurrent.thread;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ThreadPool {

    int size();

    void shutdown();

    default boolean supportsBatching() {
        return size() > 2;
    }

    Future<?> submit(Runnable runnable);

    <T> Future<T> submit(Callable<T> callable);

    Resource<Batcher> batcher();
}
