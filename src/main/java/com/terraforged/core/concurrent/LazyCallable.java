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

package com.terraforged.core.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class LazyCallable<T> implements Callable<T>, Future<T> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private T value = null;

    @Override
    public final T call() {
        // read lock allows us to safely read from the this.value field. it will only block if another thread holds the
        // write lock (in which case we would want to wait until that has finished computing the value)
        lock.readLock().lock();
        T result = this.value;
        lock.readLock().unlock();

        // if the result is currently null then try to create it
        if (result == null) {
            // only one thread can hold the write lock so this blocks until it is obtained. this behaviour is important
            // as we do not want a second thread to start creating the value if an earlier one is in the process
            // of doing that
            lock.writeLock().lock();

            // if another thread had the write lock before us then this.value may have have been computed so read it
            // into the result variable again
            result = this.value;

            // if it's still null then actually do the computation and store it for future use
            if (result == null) {
                result = create();
                this.value = result;
            }

            lock.writeLock().unlock();
        }

        return result;
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        // not supported
        return false;
    }

    @Override
    public final boolean isCancelled() {
        // not supported
        return false;
    }

    @Override
    public final boolean isDone() {
        lock.readLock().lock();
        T result = this.value;
        lock.readLock().unlock();
        return result != null;
    }

    @Override
    public final T get() {
        return call();
    }

    @Override
    public final T get(long timeout, TimeUnit unit) {
        // not supported
        return call();
    }

    protected abstract T create();
}
