package com.terraforged.core.render;

public interface RenderAPI {

    void pushMatrix();

    void popMatrix();

    void translate(float x, float y, float z);

    void rotateX(float angle);

    void rotateY(float angle);

    void rotateZ(float angle);

    RenderBuffer createBuffer();
}
