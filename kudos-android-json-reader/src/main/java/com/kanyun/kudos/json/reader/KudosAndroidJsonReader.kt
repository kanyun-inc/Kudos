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

package com.kanyun.kudos.json.reader

import android.util.JsonReader
import com.kanyun.kudos.json.reader.adapter.KudosJsonAdapter
import com.kanyun.kudos.json.reader.adapter.parseKudosObject
import java.io.BufferedReader
import java.io.InputStream
import java.lang.reflect.Type

object KudosAndroidJsonReader {
    inline fun <reified T> fromJson(json: String): T {
        return fromJson(json.reader().buffered(), T::class.java)
    }

    inline fun <reified T> fromJson(inputStream: InputStream): T {
        return fromJson(inputStream.bufferedReader(), T::class.java)
    }

    fun <T> fromJson(bufferedReader: BufferedReader, clazz: Class<T>): T {
        val adapter = clazz.getDeclaredConstructor().newInstance()
        return if (adapter is KudosJsonAdapter<*>) {
            val jsonReader = JsonReader(bufferedReader)
            adapter.fromJson(jsonReader) as T
        } else {
            throw IllegalArgumentException("class ${clazz.name} must implement KudosJsonAdapter")
        }
    }

    fun <T> fromJson(json: String, type: Type): T {
        val jsonReader = JsonReader(json.reader().buffered())
        return parseKudosObject(jsonReader, type) as T
    }

    fun <T> fromJson(inputStream: InputStream, type: Type): T {
        val jsonReader = JsonReader(inputStream.bufferedReader())
        return parseKudosObject(jsonReader, type) as T
    }
}

const val KUDOS_ANDROID_JSON_READER: Int = 3
