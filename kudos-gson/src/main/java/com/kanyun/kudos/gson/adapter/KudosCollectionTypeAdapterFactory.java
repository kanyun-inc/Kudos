/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kanyun.kudos.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.bind.TypeAdapterUtils;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.kanyun.kudos.collections.KudosCollection;
import com.kanyun.kudos.collections.KudosList;
import com.kanyun.kudos.collections.KudosSet;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Adapt a homogeneous collection of objects.
 */
public final class KudosCollectionTypeAdapterFactory implements TypeAdapterFactory {
    @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    Type type = typeToken.getType();

    Class<? super T> rawType = typeToken.getRawType();
    if (!KudosCollection.class.isAssignableFrom(rawType)) {
      return null;
    }

    Type elementType = $Gson$Types.getCollectionElementType(type, rawType);
    TypeAdapter<?> elementTypeAdapter = gson.getAdapter(TypeToken.get(elementType));

    @SuppressWarnings({"unchecked", "rawtypes"}) // create() doesn't define a type parameter
    TypeAdapter<T> result = new Adapter(gson, elementType, elementTypeAdapter, rawType);
    return result;
  }

  private static final class Adapter<E> extends TypeAdapter<KudosCollection<E>> {
    private final TypeAdapter<E> elementTypeAdapter;
    private final Class<?> collectionType;

    public Adapter(Gson context, Type elementType, TypeAdapter<E> elementTypeAdapter, Class<KudosCollection<E>> collectionType) {
      this.elementTypeAdapter = TypeAdapterUtils.wrapAdapter(context, elementTypeAdapter, elementType);
      this.collectionType = collectionType;
    }

    @Override public KudosCollection<E> read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      KudosCollection<E> collection;
      if (collectionType == KudosSet.class) {
        collection = new KudosSet<>();
      } else {
        collection = new KudosList<>();
      }
      in.beginArray();
      while (in.hasNext()) {
        E instance = elementTypeAdapter.read(in);
        if (instance == null) {
          throw new NullPointerException("Element cannot be null for " + collectionType.getName() + ".");
        }
        collection.add(instance);
      }
      in.endArray();
      return collection;
    }

    @Override public void write(JsonWriter out, KudosCollection<E> collection) throws IOException {
      if (collection == null) {
        out.nullValue();
        return;
      }

      out.beginArray();
      for (E element : collection) {
        elementTypeAdapter.write(out, element);
      }
      out.endArray();
    }
  }
}
