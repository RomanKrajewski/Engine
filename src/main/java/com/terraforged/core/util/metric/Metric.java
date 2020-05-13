package com.terraforged.core.util.metric;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Metric {

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong nanos = new AtomicLong();

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
        return new Timer();
    }

    public class Timer implements AutoCloseable {

        private final long start = System.nanoTime();

        @Override
        public void close() {
            long duration = System.nanoTime() - start;
            nanos.addAndGet(duration);
            hits.incrementAndGet();
        }
    }
}
