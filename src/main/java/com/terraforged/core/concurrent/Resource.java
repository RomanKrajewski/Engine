package com.terraforged.core.concurrent;

import com.terraforged.core.concurrent.cache.SafeCloseable;

public interface Resource<T> extends SafeCloseable {

    T get();

    boolean isOpen();

    Resource NONE = new Resource() {
        @Override
        public Object get() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public void close() {

        }
    };

    @SuppressWarnings("unchecked")
    static <T> Resource<T> empty() {
        return (Resource<T>) NONE;
    }
}
