package com.terraforged.core.concurrent.batch;

import com.terraforged.core.concurrent.Resource;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskBatcher implements Batcher, BatchTask.Notifier, Resource<Batcher> {

    private final Executor executor;
    private final Object lock = new Object();
    private final AtomicInteger count = new AtomicInteger();

    private int size;
    private int submitted;

    public TaskBatcher(Executor executor) {
        this.executor = executor;
    }

    @Override
    public Batcher get() {
        return this;
    }

    @Override
    public void markDone() {
        if (count.incrementAndGet() >= size) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    @Override
    public void size(int size) {
        this.count.set(0);
        this.size = size;
        this.submitted = 0;
    }

    @Override
    public void submit(Runnable task) {

    }

    @Override
    public void submit(BatchTask task) {
        if (submitted < size) {
            submitted++;
            task.setNotifier(this);
            executor.execute(task);
        }
    }

    @Override
    public void close() {
        if (submitted <= 0) {
            return;
        }
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
