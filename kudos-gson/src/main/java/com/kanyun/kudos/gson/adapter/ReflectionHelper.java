package com.kanyun.kudos.gson.adapter;

import com.google.gson.JsonIOException;
import java.lang.reflect.Field;

/**
 * Created by benny at 2023/9/4 19:02.
 */
class ReflectionHelper {

    public static void makeAccessible(Field field) throws JsonIOException {
        try {
            field.setAccessible(true);
        } catch (Exception exception) {
            throw new JsonIOException("Failed making field '" + field.getDeclaringClass().getName() + "#"
                    + field.getName() + "' accessible; either change its visibility or write a custom "
                    + "TypeAdapter for its declaring type", exception);
        }
    }

}
