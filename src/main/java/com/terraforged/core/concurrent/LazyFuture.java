package com.terraforged.core.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class LazyFuture<T> implements Future<T> {

    private final Callable<T> callable;
    private volatile T value = null;

    public LazyFuture(Callable<T> callable) {
        this.callable = callable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return value != null;
    }

    private T getOrCall() throws Exception {
        if (value == null) {
            T result = callable.call();
            value = result;
            return result;
        }
        return value;
    }

    @Override
    public T get() {
        try {
            return getOrCall();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return get();
    }

    public static Future<Void> adapt(Runnable runnable) {
        return new LazyFuture<>(() -> {
            runnable.run();
            return null;
        });
    }
}
