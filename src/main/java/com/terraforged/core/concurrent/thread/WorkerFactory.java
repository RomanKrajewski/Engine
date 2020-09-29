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

import com.terraforged.core.concurrent.thread.context.ContextThread;
import com.terraforged.core.concurrent.thread.context.ContextWorkerThread;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// As DefaultThreadPool but with custom thread names
public class WorkerFactory implements ThreadFactory {

    protected final String prefix;
    protected final ThreadGroup group;
    protected final AtomicInteger threadNumber = new AtomicInteger(1);

    public WorkerFactory(String name) {
        group = Thread.currentThread().getThreadGroup();
        prefix = name + "-Worker-";
    }

    @Override
    public Thread newThread(Runnable task) {
        Thread thread = new ContextThread(group, task);
        thread.setDaemon(true);
        thread.setName(prefix + threadNumber.getAndIncrement());
        return thread;
    }

    public static class ForkJoin extends WorkerFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

        public ForkJoin(String name) {
            super(name);
        }

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread thread = new ContextWorkerThread(pool);
            thread.setDaemon(true);
            thread.setName(prefix + threadNumber.getAndIncrement());
            return thread;
        }
    }
}
