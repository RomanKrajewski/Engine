/*
 *
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

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Name;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;
import com.terraforged.core.util.NameUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Serializer {

    public static void serialize(Object object, Writer writer) throws IllegalAccessException {
        serialize(object, writer, true);
    }

    public static void serialize(Object object, Writer writer, boolean meta) throws IllegalAccessException {
        serialize(object, writer, "", meta);
    }

    public static void serialize(Object object, Writer writer, String parentId, boolean meta) throws IllegalAccessException {
        if (object.getClass().isArray()) {
            writer.beginArray();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(object, i);
                serialize(element, writer);
            }
            writer.endArray();
        } else if (!object.getClass().isPrimitive()) {
            int order = 0;
            writer.beginObject();
            for (Field field : object.getClass().getFields()) {
                if (Serializer.isSerializable(field)) {
                    field.setAccessible(true);
                    write(object, field, order, writer, parentId, meta);
                    order++;
                }
            }
            writer.endObject();
        }
    }

    private static void write(Object object, Field field, int order, Writer writer, String parentId, boolean meta) throws IllegalAccessException {
        if (field.getType() == int.class) {
            writer.name(field.getName()).value((int) field.get(object));
            writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType() == float.class) {
            writer.name(field.getName()).value((float) field.get(object));
            writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType() == String.class) {
            writer.name(field.getName()).value((String) field.get(object));
            writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType() == boolean.class) {
            writer.name(field.getName()).value("" + (field.get(object)));
            writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType().isEnum()) {
            writer.name(field.getName()).value(((Enum<?>) field.get(object)).name());
            writeMeta(field, order, writer, parentId, meta);
            return;
        }
        if (field.getType().isArray()) {
            if (field.getType().getComponentType().isAnnotationPresent(Serializable.class)) {
                writer.name(field.getName());
                serialize(field.get(object), writer, getKeyName(parentId, field), meta);
                writeMeta(field, order, writer, parentId, meta);
            }
            return;
        }
        if (field.getType().isAnnotationPresent(Serializable.class)) {
            writer.name(field.getName());
            String parent = getKeyName(parentId, field);
            serialize(field.get(object), writer, parent, meta);
            writeMeta(field, order, writer, parentId, meta);
        }
    }

    private static void writeMeta(Field field, int order, Writer writer, String parentId, boolean meta) {
        if (!meta) {
            return;
        }

        writer.name("#" + field.getName()).beginObject();
        writer.name("order").value(order);
        writer.name("key").value(getKeyName(parentId, field));
        writer.name("display").value(getDisplayName(field));

        Range range = field.getAnnotation(Range.class);
        if (range != null) {
            if (field.getType() == int.class) {
                writer.name("min").value((int) range.min());
                writer.name("max").value((int) range.max());
            } else {
                writer.name("min").value(range.min());
                writer.name("max").value(range.max());
            }
        }

        Comment comment = field.getAnnotation(Comment.class);
        if (comment != null) {
            writer.name("comment");
            writer.value(comment.value()[0]);
        }

        if (field.getType() == boolean.class) {
            writer.name("options");
            writer.beginArray();
            writer.value("true");
            writer.value("false");
            writer.endArray();
        }

        if (field.getType().isEnum()) {
            writer.name("options");
            writer.beginArray();
            for (Enum<?> o : field.getType().asSubclass(Enum.class).getEnumConstants()) {
                writer.value(o.name());
            }
            writer.endArray();
        }

        writer.endObject();
    }

    private static String getDisplayName(Field field) {
        Name nameMeta = field.getAnnotation(Name.class);
        String name = nameMeta == null ? field.getName() : nameMeta.value();
        return NameUtil.toDisplayName(name);
    }

    private static String getKeyName(String parent, Field field) {
        Name nameMeta = field.getAnnotation(Name.class);
        String name = nameMeta == null ? field.getName() : nameMeta.value();
        return NameUtil.toTranslationKey(parent, name);
    }

    protected static boolean isSerializable(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers)
                && !Modifier.isFinal(modifiers)
                && !Modifier.isStatic(modifiers)
                && !Modifier.isTransient(modifiers);
    }
}
