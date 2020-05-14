package com.terraforged.core.concurrent.batcher;

public interface BatchedTask extends Runnable {

    void setBatcher(BatchNotifier notifier);
}
