package com.google.gson;

import com.google.gson.internal.ConstructorConstructor;
import java.lang.reflect.Field;

/**
 * Created by benny at 2023/9/4 18:42.
 */
public class ConstructorConstructorUtils {

    public static ConstructorConstructor getInstance(Gson gson) {
        try {
            Field field = Gson.class.getDeclaredField("constructorConstructor");
            field.setAccessible(true);
            return (ConstructorConstructor) field.get(gson);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Cannot get ConstructorConstructor from Gson. Maybe a compatibility problem, consider submitting an issue to Kudos.", e);
        }
    }

}
