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

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.batch.Batcher;
import com.terraforged.core.concurrent.batch.TaskBatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BatchingThreadPool implements ThreadPool {

    private final int size;
    private final boolean keepalive;
    private final ExecutorService taskExecutor;
    private final ExecutorService batchExecutor;

    private BatchingThreadPool(int taskSize, int batchSize, boolean keepalive) {
        this.keepalive = keepalive;
        this.size = taskSize + batchSize;
        this.taskExecutor = Executors.newFixedThreadPool(taskSize, new WorkerFactory("TF-Task"));
        this.batchExecutor = Executors.newFixedThreadPool(batchSize, new WorkerFactory("TF-Batch"));
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
    public boolean keepAlive() {
        return keepalive;
    }

    @Override
    public void shutdown() {
        taskExecutor.shutdown();
        batchExecutor.shutdown();
        ThreadPools.shutdown(this);
    }

    @Override
    public Resource<Batcher> batcher() {
        return new TaskBatcher(batchExecutor);
    }

    public static ThreadPool of(int size, boolean keepalive) {
        int tasks = Math.max(1, size / 4);
        return new BatchingThreadPool(tasks, size - tasks, keepalive);
    }
}
