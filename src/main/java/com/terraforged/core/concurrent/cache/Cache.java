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

import com.terraforged.core.concurrent.thread.ThreadPool;
import com.terraforged.core.concurrent.thread.ThreadPools;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.LongFunction;

public class Cache<V extends ExpiringEntry> implements Runnable {

    private final long expireMS;
    private final long intervalMS;
    private final SynchronizedLongMap<V> map;
    private final ThreadPool threadPool = ThreadPools.getUtilPool();

    private volatile long timestamp = 0L;

    public Cache(long expireTime, long interval, TimeUnit unit) {
        this(200, expireTime, interval, unit);
    }

    public Cache(int capacity, long expireTime, long interval, TimeUnit unit) {
        this.expireMS = unit.toMillis(expireTime);
        this.intervalMS = unit.toMillis(interval);
        this.map = new SynchronizedLongMap<>(capacity);
    }

    public void remove(long key) {
        map.remove(key, V::close);
    }

    public V get(long key) {
        return map.get(key);
    }

    public V computeIfAbsent(long key, LongFunction<V> func) {
        V v = map.computeIfAbsent(key, func);
        queueUpdate();
        return v;
    }

    public <T> T map(long key, LongFunction<V> func, Function<V, T> mapper) {
        T t = map.map(key, func, mapper);
        queueUpdate();
        return t;
    }

    private void queueUpdate() {
        long now = System.currentTimeMillis();
        if (now - timestamp > intervalMS) {
            timestamp = now;
            threadPool.submit(this);
        }
    }

    @Override
    public void run() {
        final long now = timestamp;
        map.removeIf(val -> {
            if (now - val.getTimestamp() > expireMS) {
                val.close();
                return true;
            }
            return false;
        });
    }
}
