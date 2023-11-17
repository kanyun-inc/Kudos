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

package com.kanyun.kudos.compiler.options

object Options {

    const val KUDOS_GSON = 1
    const val KUDOS_JACKSON = 2
    const val KUDOS_ANDROID_JSON_READER = 3

    @JvmField
    val gson = Option(
        "gson",
        false,
        "Whether to enable the support for Gson.",
        "<true/false>",
    )

    @JvmField
    val jackson = Option(
        "jackson",
        false,
        "Whether to enable the support for Jackson.",
        "<true/false>",
    )

    @JvmField
    val androidJsonReader = Option(
        "androidJsonReader",
        false,
        "Whether to enable the support for AndroidJsonReader.",
        "<true/false>",
    )

    val all = Options::class.java.declaredFields.filter {
        it.type == Option::class.java
    }.map {
        it.get(null) as Option<*>
    }

    fun isGsonEnabled(kudosAnnotationValueMap: HashMap<String, List<Int>>, className: String?): Boolean {
        if (className.isNullOrEmpty()) return false
        val annotationValue = kudosAnnotationValueMap[className]
        return if (annotationValue.isNullOrEmpty()) {
            gson()
        } else {
            annotationValue.contains(KUDOS_GSON)
        }
    }

    fun isAndroidJsonReaderEnabled(kudosAnnotationValueMap: HashMap<String, List<Int>>, className: String?): Boolean {
        if (className.isNullOrEmpty()) return false
        val annotationValue = kudosAnnotationValueMap[className]
        return if (annotationValue.isNullOrEmpty()) {
            androidJsonReader()
        } else {
            annotationValue.contains(KUDOS_ANDROID_JSON_READER)
        }
    }
}
