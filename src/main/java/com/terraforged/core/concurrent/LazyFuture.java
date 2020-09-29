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
import java.util.function.Supplier;

public class LazyFuture<T> extends LazyCallable<T> {

    private final Supplier<T> supplier;

    public LazyFuture(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected T create() {
        return supplier.get();
    }

    public static Future<Void> adapt(Runnable runnable) {
        return new LazyFuture<>(() -> {
            runnable.run();
            return null;
        });
    }

    public static <T> Future<T> adapt(Callable<T> callable) {
        return new LazyFuture<>(() -> {
            try {
                return callable.call();
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        });
    }
}
