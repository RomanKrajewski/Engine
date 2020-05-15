package com.terraforged.core.concurrent.pool;

import com.terraforged.core.concurrent.ObjectPool;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;
import com.terraforged.core.concurrent.batch.TaskBatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BatchingThreadPool implements ThreadPool {

    private final int size;
    private final ExecutorService taskExecutor;
    private final ExecutorService batchExecutor;

    private BatchingThreadPool(int taskSize, int batchSize) {
        size = taskSize + batchSize;
        taskExecutor = Executors.newFixedThreadPool(taskSize, new WorkerFactory("TF-Task"));
        batchExecutor = Executors.newFixedThreadPool(batchSize, new WorkerFactory("TF-Batch"));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return taskExecutor.submit(runnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return taskExecutor.submit(callable);
    }

    @Override
    public void shutdown() {
        taskExecutor.shutdown();
        batchExecutor.shutdown();
    }

    @Override
    public Resource<Batcher> batcher() {
        return new TaskBatcher(batchExecutor);
    }

    public static ThreadPool of(int size) {
        int tasks = Math.max(1, size / 4);
        return new BatchingThreadPool(tasks, size - tasks);
    }
}
