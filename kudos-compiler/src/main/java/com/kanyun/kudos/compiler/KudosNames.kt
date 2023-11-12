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

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object KudosNames {
    // const
    const val KUDOS_VALIDATOR = "com.kanyun.kudos.validator.KudosValidator"
    const val KUDOS_JSON_ADAPTER = "com.kanyun.kudos.json.reader.adapter.KudosJsonAdapter"
    const val KUDOS_IGNORE = "com.kanyun.kudos.annotations.KudosIgnore"
    const val TRANSIENT = "kotlin.jvm.Transient"

    // FqName
    val KUDOS_NAME = FqName("com.kanyun.kudos.annotations.Kudos")
    val KUDOS_VALIDATOR_NAME = FqName(KUDOS_VALIDATOR)
    val KUDOS_IGNORE_NAME = FqName(KUDOS_IGNORE)
    val TRANSIENT_NAME = FqName(TRANSIENT)

    // Avoid package relocating
    val JSON_ADAPTER_NAME = FqName("#com.google.gson.annotations.JsonAdapter".removePrefix("#"))
    val ADAPTER_FACTORY_NAME = FqName("com.kanyun.kudos.gson.adapter.KudosReflectiveTypeAdapterFactory")

    // ClassId
    val KUDOS_VALIDATOR_CLASS_ID = ClassId(FqName("com.kanyun.kudos.validator"), Name.identifier("KudosValidator"))
    val KUDOS_JSON_ADAPTER_CLASS_ID = ClassId(FqName("com.kanyun.kudos.json.reader.adapter"), Name.identifier("KudosJsonAdapter"))
    val JSON_READER_CLASS_ID = ClassId.fromString("android/util/JsonReader")
    val JSON_TOKEN_CLASS_ID = ClassId(FqName("android.util"), Name.identifier("JsonToken"))

    // CallableId
    val JSON_READER_SKIP_VALUE_CALLABLE_ID = CallableId(FqName("android.util"), FqName("JsonReader"), Name.identifier("skipValue"))
    val JSON_READER_PEEK_CALLABLE_ID = CallableId(FqName("android.util"), FqName("JsonReader"), Name.identifier("peek"))
    val JSON_TOKEN_NULL_CALLABLE_ID = CallableId(FqName("android.util"), FqName("JsonToken"), Name.identifier("NULL"))

    // Name.identifier
    val KUDOS_FROM_JSON_IDENTIFIER = Name.identifier("fromJson")
    val JSON_READER_IDENTIFIER = Name.identifier("jsonReader")
    val KUDOS_FIELD_STATUS_MAP_IDENTIFIER = Name.identifier("kudosFieldStatusMap")
    val JSON_TOKEN_NULL_IDENTIFIER = Name.identifier("NULL")

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
}
