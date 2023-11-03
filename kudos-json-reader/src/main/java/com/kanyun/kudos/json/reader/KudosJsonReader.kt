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

object KudosJsonReader {
    inline fun <reified T> fromJson(json: String): T {
        return fromJson(json, T::class.java)
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T {
        val adapter = clazz.getDeclaredConstructor().newInstance()
        return if (adapter is KudosJsonAdapter<*>) {
            val jsonReader = JsonReader(json.reader())
            adapter.fromJson(jsonReader) as T
        } else {
            throw IllegalArgumentException("class ${clazz.name} must implement KudosJsonAdapter")
        }
    }
}
