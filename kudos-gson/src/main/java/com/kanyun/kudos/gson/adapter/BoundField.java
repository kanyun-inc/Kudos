package com.kanyun.kudos.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BoundField {

    public final Field field;

    public final TypeAdapter typeAdapter;

    public final String name;
    public final boolean serialized;
    public final boolean deserialized;

    boolean isInitialized = false;

    public BoundField(Field field, TypeAdapter typeAdapter, String name, boolean serialized, boolean deserialized) {
        this.field = field;
        this.typeAdapter = typeAdapter;
        this.name = name;
        this.serialized = serialized;
        this.deserialized = deserialized;
    }

    public boolean writeField(Object value) throws IOException, IllegalAccessException {
        if (!serialized) return false;
        Object fieldValue = field.get(value);
        return fieldValue != value; // avoid recursion for example for Throwable.cause
    }

    public void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
        Object fieldValue = field.get(value);
        typeAdapter.write(writer, fieldValue);
    }

    public void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
        Object fieldValue = typeAdapter.read(reader);
        if (fieldValue != null) {
            isInitialized = true;
        }
        if (fieldValue != null || !field.getType().isPrimitive()) {
            field.set(value, fieldValue);
        }
    }
}
