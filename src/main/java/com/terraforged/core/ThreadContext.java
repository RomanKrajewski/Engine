package com.terraforged.core;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.SimpleResource;

// used to attach high-demand resources to TF controlled worker threads
public class ThreadContext {
    
    public final Resource<Cell> cell = new SimpleResource<>(new Cell(), Cell::reset);
}
