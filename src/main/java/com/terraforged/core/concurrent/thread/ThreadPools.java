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
            return new ForkJoinThreadPool(poolSize);
        }
        return BatchingThreadPool.of(poolSize);
    }

    public static ThreadPool createDefault() {
        return create(defaultPoolSize());
    }

    public static ThreadPool create(int poolSize) {
        return create(poolSize, poolSize < 4);
    }

    public static ThreadPool create(int poolSize, boolean batching) {
        synchronized (lock) {
            ThreadPool current = instance.get();

            if (current != null) {
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
            return setAndGet(new ForkJoinThreadPool(poolSize));
        }

        return setAndGet(BatchingThreadPool.of(poolSize));
    }

    private static ThreadPool setAndGet(ThreadPool threadPool) {
        synchronized (lock) {
            instance = new WeakReference<>(threadPool);
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
