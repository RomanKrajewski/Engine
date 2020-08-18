/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.core.serialization.serializer;

public abstract class AbstractWriter<T, S extends AbstractWriter<T, ?>> implements Writer {

    private final Context<T> root = new Context<>(null);

    private String name = "";
    private Context<T> context = root;

    public T getRoot() {
        return root.value;
    }

    @Override
    public S name(String name) {
        this.name = name;
        return self();
    }

    @Override
    public S beginObject() {
        begin(createObject());
        return self();
    }

    @Override
    public S endObject() {
        context = context.parent;
        return self();
    }

    @Override
    public S beginArray() {
        begin(createArray());
        return self();
    }

    @Override
    public S endArray() {
        context = context.parent;
        return self();
    }

    @Override
    public S value(String value) {
        append(create(value));
        return self();
    }

    @Override
    public S value(float value) {
        append(create(value));
        return self();
    }

    @Override
    public S value(int value) {
        append(create(value));
        return self();
    }

    private void begin(T value) {
        if (root.value == null) {
            root.value = value;
            context.value = value;
        } else {
            append(value);
            context = new Context<>(context);
            context.value = value;
        }
    }

    private void append(T value) {
        if (isObject(context.value)) {
            add(context.value, name, value);
        } else if (isArray(context.value)) {
            add(context.value, value);
        }
    }

    protected abstract S self();

    protected abstract boolean isObject(T value);

    protected abstract boolean isArray(T value);

    protected abstract void add(T parent, String key, T value);

    protected abstract void add(T parent, T value);

    protected abstract T createObject();

    protected abstract T createArray();

    protected abstract T create(String value);

    protected abstract T create(int value);

    protected abstract T create(float value);

    private static class Context<T> {

        private final Context<T> parent;
        private T value;

        private Context(Context<T> root) {
            this.parent = root;
        }
    }
}
