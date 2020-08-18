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

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.function.Function;

public class CacheEntry<T> implements ExpiringEntry {

    private volatile long timestamp;
    private volatile T value = null;

    private final Future<T> task;

    public CacheEntry(Future<T> task) {
        this.task = task;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void close() {
        if (value instanceof AutoCloseable) {
            try {
                ((AutoCloseable) value).close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isDone() {
        return task.isDone();
    }

    public T get() {
        // update each time accessed
        this.timestamp = System.currentTimeMillis();

        if (task instanceof ForkJoinTask) {
            return value = ((ForkJoinTask<T>) task).join();
        }

        try {
            return value = task.get();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public <V> CacheEntry<V> then(ThreadPool executor, Function<T, V> function) {
        return supplyAsync(() -> function.apply(get()), executor);
    }

    public static <T> CacheEntry<T> supply(Future<T> task) {
        return new CacheEntry<>(task);
    }

    public static <T> CacheEntry<T> supplyAsync(Callable<T> callable, ThreadPool executor) {
        return new CacheEntry<>(executor.submit(callable));
    }
}
