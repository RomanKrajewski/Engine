package com.terraforged.core.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class LazyFuture<T> extends LazyCallable<T> {

    private final Supplier<T> supplier;

    public LazyFuture(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected T create() {
        return supplier.get();
    }

    public static Future<Void> adapt(Runnable runnable) {
        return new LazyFuture<>(() -> {
            runnable.run();
            return null;
        });
    }

    public static <T> Future<T> adapt(Callable<T> callable) {
        return new LazyFuture<>(() -> {
            try {
                return callable.call();
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        });
    }
}
