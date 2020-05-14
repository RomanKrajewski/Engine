package com.terraforged.core.concurrent.batcher;

public interface BatchNotifier {

    BatchNotifier NONE = () -> {};

    void markDone();
}
