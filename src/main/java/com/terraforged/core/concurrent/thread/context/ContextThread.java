package com.terraforged.core.concurrent.thread.context;

import com.terraforged.core.ThreadContext;

public class ContextThread extends Thread implements ContextualThread {

    private final ThreadContext context;

    public ContextThread(ThreadGroup group, Runnable runnable) {
        this(group, runnable, new ThreadContext());
    }

    public ContextThread(ThreadGroup group, Runnable runnable, ThreadContext context) {
        super(group, runnable);
        this.context = context;
    }

    @Override
    public ThreadContext getContext() {
        return context;
    }
}
