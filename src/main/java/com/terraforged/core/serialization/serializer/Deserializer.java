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

import com.terraforged.core.serialization.annotation.Serializable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class Deserializer {

    public static boolean deserialize(Reader reader, Object object) throws Throwable {
        boolean valid = true;
        Class<?> type = object.getClass();
        for (String name : reader.getKeys()) {
            if (name.charAt(0) == '#') {
                continue;
            }

            try {
                Field field = type.getField(name);
                if (Serializer.isSerializable(field)) {
                    field.setAccessible(true);
                    valid &= fromValue(reader, object, field);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                valid = false;
            }
        }
        return valid;
    }

    private static boolean fromValue(Reader reader, Object object, Field field) throws Throwable {
        if (field.getType() == int.class) {
            field.set(object, reader.getInt(field.getName()));
            return true;
        }
        if (field.getType() == float.class) {
            field.set(object, reader.getFloat(field.getName()));
            return true;
        }
        if (field.getType() == boolean.class) {
            field.set(object, reader.getString(field.getName()).equals("true"));
            return true;
        }
        if (field.getType() == String.class) {
            field.set(object, reader.getString(field.getName()));
            return true;
        }
        if (field.getType().isEnum()) {
            String name = reader.getString(field.getName());
            for (Enum<?> e : field.getType().asSubclass(Enum.class).getEnumConstants()) {
                if (e.name().equals(name)) {
                    field.set(object, e);
                    return true;
                }
            }
            return false;
        }
        if (field.getType().isAnnotationPresent(Serializable.class)) {
            Reader child = reader.getChild(field.getName());
            Object value = field.getType().newInstance();
            deserialize(child, value);
            field.set(object, value);
            return true;
        }
        if (field.getType().isArray()) {
            Class<?> type = field.getType().getComponentType();
            if (type.isAnnotationPresent(Serializable.class)) {
                Reader child = reader.getChild(field.getName());
                Object array = Array.newInstance(type, child.getSize());
                for (int i = 0; i < child.getSize(); i++) {
                    Object value = type.newInstance();
                    deserialize(child.getChild(i), value);
                    Array.set(array, i, value);
                }
                field.set(object, array);
            }
        }
        return true;
    }
}
