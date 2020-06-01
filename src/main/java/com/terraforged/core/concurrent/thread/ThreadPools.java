package com.terraforged.core.concurrent.thread;

public class ThreadPools {

    private static final ThreadPool instance = create(defaultPoolSize());

    public static ThreadPool getPool() {
        return instance;
    }

    public static ThreadPool create(int poolSize) {
        if (poolSize == 1) {
            return new SingleThreadPool();
        }
        if (poolSize < 4) {
            return new ForkJoinThreadPool(poolSize);
        }
        return BatchingThreadPool.of(poolSize);
    }

    public static int defaultPoolSize() {
        int processors = Runtime.getRuntime().availableProcessors();
        return Math.max(1, (processors / 3) * 2);
    }
}
