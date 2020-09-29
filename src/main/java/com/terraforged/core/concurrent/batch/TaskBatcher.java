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
    public boolean isOpen() {
        return submitted > 0;
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
