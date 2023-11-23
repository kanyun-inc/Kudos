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

package com.kanyun.kudos.validator

/**
 * Created by Benny Huo on 2023/9/8
 */
interface KudosValidator {
    fun validate(status: Map<String, Boolean>) {}
}

fun validateField(name: String, fieldStatus: Map<String, Boolean>) {
    if (fieldStatus[name] != true) {
        throw NullPointerException("Missing non-null field '$name'.")
    }
}

fun validateCollection(name: String, collection: Collection<*>?, typeName: String? = null) {
    if (collection == null) return
    if (collection.any { it == null }) {
        throw java.lang.NullPointerException("Element must not be null in ${typeName ?: collection.javaClass.simpleName} '$name'.")
    }
}

fun validateArray(name: String, array: Array<*>?) {
    if (array == null) return
    if (array.any { it == null }) {
        throw java.lang.NullPointerException("Element must not be null in array '$name'.")
    }
}
