/*
 *   
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

package com.terraforged.core.concurrent.batcher;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncBatcher implements Batcher {

    private final ForkJoinPool pool;
    private final Object notifier = new Object();

    private int size = 0;
    private int submitted = 0;
    private final AtomicInteger count = new AtomicInteger();

    public AsyncBatcher(ForkJoinPool pool) {
        this.pool = pool;
    }

    @Override
    public void markDone() {
        if (count.incrementAndGet() >= size) {
            synchronized (notifier) {
                notifier.notifyAll();
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
    public void submit(BatchedTask task) {
        if (++submitted > size) {
            throw new RuntimeException("Exceeded batch size. Submitted: " + submitted + ", Max Allowed: " + size);
        }
        task.setBatcher(this);
        pool.submit(task);
    }

    @Override
    public void close() {
        synchronized (notifier) {
            try {
                notifier.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
