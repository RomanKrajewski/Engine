package com.terraforged.core.concurrent.cache;

import com.terraforged.core.concurrent.pool.ThreadPool;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.function.Function;

public class CacheEntry<T> implements ExpiringEntry {

    private volatile long timestamp;
    private final Future<T> task;

    public CacheEntry(Future<T> task) {
        this.task = task;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public boolean isDone() {
        return task.isDone();
    }

    public T get() {
        if (task instanceof ForkJoinTask) {
            return ((ForkJoinTask<T>) task).join();
        }
        try {
            return task.get();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public <V> CacheEntry<V> then(ThreadPool executor, Function<T, V> function) {
        return supplyAsync(() -> function.apply(get()), executor);
    }

    public static <T> CacheEntry<T> supply(Future<T> task) {
        return new CacheEntry<>(task);
    }

    public static <T> CacheEntry<T> supplyAsync(Callable<T> callable, ThreadPool executor) {
        return new CacheEntry<>(executor.submit(callable));
    }
}
