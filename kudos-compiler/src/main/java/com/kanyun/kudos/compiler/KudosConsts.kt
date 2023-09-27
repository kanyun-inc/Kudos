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

package com.kanyun.kudos.compiler

/**
 * Created by Benny Huo on 2023/8/21
 */
const val KUDOS = "com.kanyun.kudos.annotations.Kudos"
const val KUDOS_IGNORE = "com.kanyun.kudos.annotations.KudosIgnore"
const val KUDOS_META = "com.kanyun.kudos.annotations.KudosMeta"
const val KUDOS_VALIDATOR = "com.kanyun.kudos.validator.KudosValidator"
const val VALIDATE_FIELD = "com.kanyun.kudos.validator.validateField"
const val VALIDATE_COLLECTION = "com.kanyun.kudos.validator.validateCollection"
const val VALIDATE_ARRAY = "com.kanyun.kudos.validator.validateArray"

const val TRANSIENT = "kotlin.jvm.Transient"

const val ADAPTER_FACTORY = "com.kanyun.kudos.gson.adapter.KudosReflectiveTypeAdapterFactory"

// Avoid package relocating
val JSON_ADAPTER = "#com.google.gson.annotations.JsonAdapter".removePrefix("#")

val CONTAINER_FQ_NAMES = setOf(
    "kotlin.Array",

    "kotlin.collections.Collection",
    "kotlin.collections.MutableCollection",

    "kotlin.collections.List",
    "kotlin.collections.MutableList",
    "kotlin.collections.ArrayList",

    "kotlin.collections.Set",
    "kotlin.collections.MutableSet",
    "kotlin.collections.HashSet",
    "kotlin.collections.LinkedHashSet",

    "kotlin.collections.Map",
    "kotlin.collections.MutableMap",
    "kotlin.collections.HashMap",
    "kotlin.collections.LinkedHashMap",
)

fun String.toNonNullContainerType(): String? {
    return if (this in CONTAINER_FQ_NAMES) {
        "com.kanyun.kudos.runtime.collections.NonNull${removePrefix("kotlin.collections.")}"
    } else {
        null
    }
}
