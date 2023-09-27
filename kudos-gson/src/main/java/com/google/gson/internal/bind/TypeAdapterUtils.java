package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Created by Benny Huo on 2023/8/22
 */
public class TypeAdapterUtils {

    public static <T> TypeAdapter<T> wrapAdapter(Gson gson, TypeAdapter<T> delegate, Type type) {
        return new TypeAdapterRuntimeTypeWrapper<T>(gson, delegate, type);
    }

    public static TypeAdapter<?> getTypeAdapter(
            JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory,
            ConstructorConstructor constructorConstructor,
            Gson gson,
            TypeToken<?> type, JsonAdapter annotation
    ) {
        return jsonAdapterFactory.getTypeAdapter(constructorConstructor, gson, type, annotation);
    }

}
