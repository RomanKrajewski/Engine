package com.terraforged.core.util.metric;

import com.terraforged.core.concurrent.cache.SafeCloseable;
import com.terraforged.core.concurrent.pool.ObjectPool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Metric {

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong nanos = new AtomicLong();
    private final ObjectPool<Timer> pool = new ObjectPool<>(4, Timer::new);

    public long hits() {
        return hits.get();
    }

    public long nanos() {
        return nanos.get();
    }

    public String average() {
        long hits = hits();
        double milli = TimeUnit.NANOSECONDS.toMillis(nanos());
        double average = milli / hits;
        return String.format("Average: %.3f", average);
    }

    public Timer timer() {
        return pool.get().get().punchIn();
    }

    public class Timer implements SafeCloseable {

        private long start = -1;

        public Timer punchIn() {
            start = System.nanoTime();
            return this;
        }

        public Timer punchOut() {
            if (start > -1) {
                long duration = System.nanoTime() - start;
                nanos.addAndGet(duration);
                hits.incrementAndGet();
                start = -1;
            }
            return this;
        }

        @Override
        public void close() {
            punchOut();
        }
    }
}
