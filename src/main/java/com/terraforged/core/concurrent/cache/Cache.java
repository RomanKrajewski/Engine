package com.terraforged.core.concurrent.cache;

import com.terraforged.core.concurrent.thread.ThreadPool;
import com.terraforged.core.concurrent.thread.ThreadPools;

import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

public class Cache<V extends ExpiringEntry> implements Runnable {

    private final long expireMS;
    private final long intervalMS;
    private final SynchronizedLongMap<V> map;
    private final ThreadPool threadPool = ThreadPools.getPool();

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
