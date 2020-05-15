package com.terraforged.core.concurrent;

public interface Resource<T> extends AutoCloseable {

    T get();

    void close();
}
