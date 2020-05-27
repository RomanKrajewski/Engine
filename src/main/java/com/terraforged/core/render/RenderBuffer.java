package com.terraforged.core.render;

public interface RenderBuffer {

    void beginQuads();

    void endQuads();

    void vertex(float x, float y, float z);

    void color(float hue, float saturation, float brightness);

    void draw();

    default void dispose() {

    }

    default void noFill() {

    }

    default void noStroke() {

    }
}
