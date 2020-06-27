package com.terraforged.core.concurrent.thread.context;

import com.terraforged.core.ThreadContext;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.Supplier;

public class ContextWorkerThread extends ForkJoinWorkerThread implements ContextualThread {

    private final ThreadContext context;

    public ContextWorkerThread(ForkJoinPool pool) {
        this(pool, ThreadContext::new);
    }

    public ContextWorkerThread(ForkJoinPool pool, Supplier<ThreadContext> supplier) {
        super(pool);
        context = supplier.get();
    }

    @Override
    public ThreadContext getContext() {
        return context;
    }
}
