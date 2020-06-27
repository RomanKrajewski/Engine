package com.terraforged.core.concurrent.thread.context;

import com.terraforged.core.ThreadContext;

public interface ContextualThread {

    ThreadContext getContext();
}
