package com.terraforged.core.concurrent.batch;

public interface BatchTask extends Runnable {

    Notifier NONE = () -> {};

    void setNotifier(Notifier notifier);

    interface Notifier {

        void markDone();
    }
}
