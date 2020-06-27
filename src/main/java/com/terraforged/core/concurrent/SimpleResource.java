package com.terraforged.core.concurrent;

import java.util.function.Consumer;

public class SimpleResource<T> implements Resource<T> {

    private final T value;
    private final Consumer<T> closer;

    private long useCount = 0L;
    private boolean open = false;

    public SimpleResource(T value, Consumer<T> closer) {
        this.value = value;
        this.closer = closer;
    }

    @Override
    public T get() {
        open = true;
        useCount++;
        return value;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        if (open) {
            open = false;
            closer.accept(value);
        }
    }

    private void log() {
        if ((useCount & 15) == 0) {
            System.out.println(useCount);
        }
    }
}
