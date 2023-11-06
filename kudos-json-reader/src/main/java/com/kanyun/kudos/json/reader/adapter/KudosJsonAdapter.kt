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

private fun parseKudosArray(jsonReader: JsonReader, typeArguments: Array<Type>): Any {
    val list = parseKudosList(jsonReader, typeArguments)
    val array = java.lang.reflect.Array.newInstance(typeArguments[0] as Class<*>, list.size)
    for (i in list.indices) {
        java.lang.reflect.Array.set(array, i, list[i])
    }
    return array
}

private fun parseKudosMap(jsonReader: JsonReader, typeArguments: Array<Type>): Map<String, Any> {
    val resultMap = mutableMapOf<String, Any>()
    jsonReader.beginObject()
    while (jsonReader.hasNext()) {
        val key = jsonReader.nextName()
        val value = parseKudosObject(jsonReader, typeArguments[1])
        resultMap[key] = value
    }
    jsonReader.endObject()
    return resultMap
}

private fun parseKudosObjectInternal(
    jsonReader: JsonReader,
    type: Type,
    typeArguments: Array<Type>,
): Any {
    val value = when (type) {
        String::class.javaObjectType -> jsonReader.nextString()
        Int::class.javaObjectType -> jsonReader.nextInt()
        Long::class.javaObjectType -> jsonReader.nextLong()
        Double::class.javaObjectType -> jsonReader.nextDouble()
        Float::class.javaObjectType -> jsonReader.nextString().toFloat()
        Boolean::class.javaObjectType -> jsonReader.nextBoolean()
        List::class.javaObjectType -> parseKudosList(jsonReader, typeArguments)
        Set::class.javaObjectType -> parseKudosList(jsonReader, typeArguments).toSet()
        Map::class.javaObjectType -> parseKudosMap(jsonReader, typeArguments)
        else -> {
            parseKudosObjectSpecial(jsonReader, type, typeArguments)
        }
    }
    return value
}

private fun parseKudosObjectSpecial(
    jsonReader: JsonReader,
    type: Type,
    typeArguments: Array<Type>,
): Any {
    return if (type.typeName.endsWith("[]")) {
        parseKudosArray(jsonReader, typeArguments)
    } else if (type is Class<*>) {
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
