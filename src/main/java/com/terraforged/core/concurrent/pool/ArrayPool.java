package com.terraforged.core.concurrent.pool;

import com.terraforged.core.concurrent.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ArrayPool<T> {

    private final int capacity;
    private final IntFunction<T[]> constructor;
    private final List<ArrayPool.Item<T>> pool;
    private final Object lock = new Object();

    public ArrayPool(int size, IntFunction<T[]> constructor) {
        this.capacity = size;
        this.constructor = constructor;
        this.pool = new ArrayList<>(size);
    }

    public Resource<T[]> get(int arraySize) {
        synchronized (lock) {
            if (pool.size() > 0) {
                ArrayPool.Item<T> resource = pool.remove(pool.size() - 1);
                if (resource.get().length >= arraySize) {
                    return resource.retain();
                }
            }
        }
        return new ArrayPool.Item<>(constructor.apply(arraySize), this);
    }

    private boolean restore(ArrayPool.Item<T> item) {
        synchronized (lock) {
            if (pool.size() < capacity) {
                pool.add(item);
                return true;
            }
        }
        return false;
    }

    public static class Item<T> implements Resource<T[]> {

        private final T[] value;
        private final ArrayPool<T> pool;

        private boolean released = false;

        private Item(T[] value, ArrayPool<T> pool) {
            this.value = value;
            this.pool = pool;
        }

        @Override
        public T[] get() {
            return value;
        }

        @Override
        public boolean isOpen() {
            return !released;
        }

        @Override
        public void close() {
            if (!released) {
                released = true;
                released = pool.restore(this);
            }
        }

        private ArrayPool.Item<T> retain() {
            released = false;
            return this;
        }
    }

    public static <T> ArrayPool<T> of(int size, IntFunction<T[]> constructor) {
        return new ArrayPool<>(size, constructor);
    }

    public static <T> ArrayPool<T> of(int size, Supplier<T> supplier, IntFunction<T[]> constructor) {
        return new ArrayPool<>(size, new ArrayConstructor<>(supplier, constructor));
    }

    private static class ArrayConstructor<T> implements IntFunction<T[]> {

        private final Supplier<T> element;
        private final IntFunction<T[]> array;

        private ArrayConstructor(Supplier<T> element, IntFunction<T[]> array) {
            this.element = element;
            this.array = array;
        }

        @Override
        public T[] apply(int size) {
            T[] t = array.apply(size);
            for (int i = 0; i < t.length; i++) {
                t[i] = element.get();
            }
            return t;
        }
    }
}
