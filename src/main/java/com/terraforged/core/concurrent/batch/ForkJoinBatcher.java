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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ForkJoinBatcher implements Batcher {

    private static final ForkJoinTask<?>[] empty = {};

    private final ForkJoinPool pool;

    private int size = 0;
    private int count = 0;
    private ForkJoinTask<?>[] tasks = empty;

    public ForkJoinBatcher(ForkJoinPool pool) {
        this.pool = pool;
    }

    @Override
    public void size(int newSize) {
        if (tasks.length < newSize) {
            count = 0;
            size = newSize;
            tasks = new ForkJoinTask[newSize];
        }
    }

    @Override
    public void submit(Runnable task) {
        if (count < size) {
            tasks[count++] = pool.submit(task);
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < size; i++) {
            tasks[i].quietlyJoin();
            tasks[i] = null;
        }
    }
}
