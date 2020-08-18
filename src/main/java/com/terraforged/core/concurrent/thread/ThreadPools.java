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

package com.terraforged.core.concurrent.thread;

import java.lang.ref.WeakReference;

public class ThreadPools {

    private static final Object lock = new Object();
    private static final ThreadPool util = createInitialPool(2);
    private static WeakReference<ThreadPool> instance = new WeakReference<>(null);

    public static ThreadPool getUtilPool() {
        return util;
    }

    private static ThreadPool createInitialPool(int poolSize) {
        if (poolSize == 1) {
            return new SingleThreadPool();
        }
        if (poolSize < 4) {
            return new ForkJoinThreadPool(poolSize, true);
        }
        return BatchingThreadPool.of(poolSize, true);
    }

    public static ThreadPool createDefault() {
        return create(defaultPoolSize());
    }

    public static ThreadPool create(int poolSize) {
        return create(poolSize, poolSize < 4);
    }

    public static ThreadPool create(int poolSize, boolean batching) {
        return create(poolSize, batching, false);
    }

    public static ThreadPool create(int poolSize, boolean batching, boolean keepAlive) {
        synchronized (lock) {
            ThreadPool current = instance.get();

            if (current != null && !current.keepAlive()) {
                if (poolSize == current.size() && current.supportsBatching() == batching) {
                    return current;
                }
                current.shutdown();
            }
        }

        if (poolSize == 1) {
            return setAndGet(new SingleThreadPool());
        }

        if (poolSize < 4 || !batching) {
            return setAndGet(new ForkJoinThreadPool(poolSize, keepAlive));
        }

        return setAndGet(BatchingThreadPool.of(poolSize, keepAlive));
    }

    private static ThreadPool setAndGet(ThreadPool threadPool) {
        if (!threadPool.keepAlive()) {
            synchronized (lock) {
                instance = new WeakReference<>(threadPool);
            }
        }
        return threadPool;
    }

    public static int defaultPoolSize() {
        int processors = Runtime.getRuntime().availableProcessors();
        return Math.max(1, (processors / 3) * 2);
    }

    public static void shutdown(ThreadPool threadPool) {
        synchronized (lock) {
            if (threadPool == instance.get()) {
                instance = new WeakReference<>(null);
            }
        }
    }
}
