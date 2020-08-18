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
