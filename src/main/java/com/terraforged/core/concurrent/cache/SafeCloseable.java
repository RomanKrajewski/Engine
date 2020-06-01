package com.terraforged.core.concurrent.cache;

public interface SafeCloseable extends AutoCloseable {

    void close();
}
