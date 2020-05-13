package com.terraforged.core.concurrent;

public interface Disposable {

    void dispose();

    interface Listener<T> {

        void onDispose(T t);
    }
}
