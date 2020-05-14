package com.terraforged.core.concurrent.batcher;

import java.util.concurrent.Callable;

public class BatchTask implements Callable<Void> {

    private final Batcher batcher;
    private final Runnable runnable;
    private final Callable<?> callable;

    public BatchTask(Callable<?> task, Batcher batcher) {
        this.batcher = batcher;
        this.callable = task;
        this.runnable = null;
    }

    public BatchTask(Runnable runnable, Batcher batcher) {
        this.runnable = runnable;
        this.callable = null;
        this.batcher = batcher;
    }

    @Override
    public Void call() throws Exception {
        try {
            if (callable != null) {
                callable.call();
                return null;
            }
            if (runnable != null) {
                runnable.run();
                return null;
            }
        } finally {
            batcher.markDone();
        }
        return null;
    }
}
