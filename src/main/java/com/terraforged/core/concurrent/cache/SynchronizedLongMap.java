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

package com.terraforged.core.concurrent.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Predicate;

public class SynchronizedLongMap<V> {

    private final Object lock;
    private final Long2ObjectOpenHashMap<V> map;

    public SynchronizedLongMap(int size) {
        this.map = new Long2ObjectOpenHashMap<>(size);
        lock = this;
    }

    public void remove(long key) {
        synchronized (lock) {
            map.remove(key);
        }
    }

    public void remove(long key, Consumer<V> consumer) {
        synchronized (lock) {
            V v = map.remove(key);
            if (v != null) {
                consumer.accept(v);
            }
        }
    }

    public void put(long key, V v) {
        synchronized (lock) {
            map.put(key, v);
        }
    }

    public V get(long key) {
        synchronized (lock) {
            return map.get(key);
        }
    }

    public V computeIfAbsent(long key, LongFunction<V> func) {
        synchronized (lock) {
            return map.computeIfAbsent(key, func);
        }
    }

    public <T> T map(long key, LongFunction<V> func, Function<V, T> mapper) {
        synchronized (lock) {
            return mapper.apply(map.computeIfAbsent(key, func));
        }
    }

    public void removeIf(Predicate<V> predicate) {
        synchronized (lock) {
            ObjectIterator<Long2ObjectMap.Entry<V>> iterator = map.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                if (predicate.test(iterator.next().getValue())) {
                    iterator.remove();
                }
            }
        }
    }
}
