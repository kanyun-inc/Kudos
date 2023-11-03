/*
 * Copyright (C) 2023 Kanyun, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kanyun.kudos.json.reader.adapter

import android.util.JsonReader
import java.lang.reflect.Type

interface KudosJsonAdapter<T> {
    fun fromJson(jsonReader: JsonReader): T
}

fun parseKudosObject(jsonReader: JsonReader, type: Type): Any {
    return if (type is ParameterizedTypeImpl) {
        parseKudosObjectInternal(jsonReader, type.rawType, type.actualTypeArguments)
    } else {
        parseKudosObjectInternal(jsonReader, type, arrayOf())
    }
}

private fun parseKudosList(jsonReader: JsonReader, typeArguments: Array<Type>): List<Any> {
    val list = mutableListOf<Any>()
    jsonReader.beginArray()
    while (jsonReader.hasNext()) {
        list.add(parseKudosObject(jsonReader, typeArguments[0]))
    }
    jsonReader.endArray()
    return list
}

private fun parseKudosObjectInternal(
    jsonReader: JsonReader,
    type: Type,
    typeArguments: Array<Type>,
): Any {
    val value = when (type) {
        String::class.java -> jsonReader.nextString()
        Int::class.java -> jsonReader.nextInt()
        Long::class.java -> jsonReader.nextLong()
        Double::class.java -> jsonReader.nextDouble()
        Boolean::class.java -> jsonReader.nextBoolean()
        List::class.java -> parseKudosList(jsonReader, typeArguments)
        Array::class.java -> parseKudosList(jsonReader, typeArguments).toTypedArray()
        else -> {
            if (type is Class<*>) {
                val adapter = type.getDeclaredConstructor().newInstance()
                if (adapter is KudosJsonAdapter<*>) {
                    adapter.fromJson(jsonReader)!!
                } else {
                    throw IllegalArgumentException("class ${type.name} must implement KudosJsonAdapter")
                }
            } else {
                throw IllegalArgumentException("class ${type.typeName} must implement KudosJsonAdapter")
            }
        }
    }
    return value
}
