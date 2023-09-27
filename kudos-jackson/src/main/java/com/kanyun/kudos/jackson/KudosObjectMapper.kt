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

package com.kanyun.kudos.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider
import com.kanyun.kudos.jackson.deserializer.KudosBeanDeserializerFactory

/**
 * Created by Benny Huo on 2023/8/28
 */
fun kudosObjectMapper(
    jf: JsonFactory? = null,
    sp: DefaultSerializerProvider? = null,
    dc: DefaultDeserializationContext? = null,
): ObjectMapper {
    val deserializationContext = if (dc != null) {
        val originalFactory = dc.factory
        if (originalFactory != null) {
            dc.with(KudosBeanDeserializerFactory(src = originalFactory))
        } else {
            dc.with(KudosBeanDeserializerFactory())
        }
    } else {
        DefaultDeserializationContext.Impl(KudosBeanDeserializerFactory())
    }
    return ObjectMapper(jf, sp, deserializationContext)
}
