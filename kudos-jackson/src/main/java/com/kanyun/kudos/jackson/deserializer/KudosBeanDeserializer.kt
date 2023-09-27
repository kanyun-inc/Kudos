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

package com.kanyun.kudos.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.BeanDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase
import com.fasterxml.jackson.databind.deser.SettableBeanProperty
import com.fasterxml.jackson.databind.deser.impl.FieldProperty
import com.kanyun.kudos.validator.KudosValidator

/**
 * Created by Benny Huo on 2023/8/25
 */
class KudosBeanDeserializer(src: BeanDeserializerBase?) : BeanDeserializer(src) {

    private val isKudosType = _beanType.isTypeOrSubTypeOf(KudosValidator::class.java)

    init {
        if (isKudosType) {
            _beanProperties.forEach {
                if (it is FieldProperty) {
                    _beanProperties.replace(it, PropertyWrapper(it))
                }
            }
        }
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Any {
        return super.deserialize(p, ctxt).also(::validateProperties)
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?, bean: Any?): Any {
        return super.deserialize(p, ctxt, bean).also(::validateProperties)
    }

    private fun validateProperties(bean: Any) {
        if (bean is KudosValidator) {
            val status = HashMap<String, Boolean>()
            for (property in properties()) {
                if (property is PropertyWrapper) {
                    status[property.name] = property.isInitialized
                }
            }

            bean.validate(status)
        }
    }

    private class PropertyWrapper(delegate: SettableBeanProperty) : SettableBeanProperty.Delegating(delegate) {

        var isInitialized = false

        override fun withDelegate(d: SettableBeanProperty): SettableBeanProperty {
            return PropertyWrapper(d)
        }

        override fun set(instance: Any?, value: Any?) {
            super.set(instance, value)
            isInitialized = true
        }

        override fun setAndReturn(instance: Any?, value: Any?): Any {
            return super.setAndReturn(instance, value).also { isInitialized = true }
        }

        override fun deserializeSetAndReturn(p: JsonParser?, ctxt: DeserializationContext?, instance: Any?): Any {
            return super.deserializeSetAndReturn(p, ctxt, instance).also { isInitialized = true }
        }

        override fun deserializeAndSet(p: JsonParser?, ctxt: DeserializationContext?, instance: Any?) {
            super.deserializeAndSet(p, ctxt, instance)
            isInitialized = true
        }
    }
}
