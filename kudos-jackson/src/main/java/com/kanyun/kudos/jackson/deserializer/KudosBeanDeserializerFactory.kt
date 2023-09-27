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

import com.fasterxml.jackson.databind.AbstractTypeResolver
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig
import com.fasterxml.jackson.databind.deser.BeanDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.DeserializerFactory
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.deser.KeyDeserializers
import com.fasterxml.jackson.databind.deser.ValueInstantiator
import com.fasterxml.jackson.databind.deser.ValueInstantiators
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.type.ArrayType
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.MapLikeType
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.ReferenceType
import com.kanyun.kudos.collections.KudosCollection
import com.kanyun.kudos.collections.KudosList

/**
 * Created by Benny Huo on 2023/8/25
 */
class KudosBeanDeserializerFactory(
    config: DeserializerFactoryConfig? = null,
    src: DeserializerFactory? = null,
) : DeserializerFactory() {

    private val delegate: DeserializerFactory = src ?: BeanDeserializerFactory(config ?: DeserializerFactoryConfig())

    override fun createBeanDeserializer(
        ctxt: DeserializationContext?,
        type: JavaType?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<Any> {
        return delegate.createBeanDeserializer(ctxt, type, beanDesc).let {
            if (it is BeanDeserializer) {
                KudosBeanDeserializer(it)
            } else {
                it
            }
        }
    }

    override fun withAdditionalDeserializers(additional: Deserializers?): DeserializerFactory? {
        return delegate.withAdditionalDeserializers(additional)
    }

    override fun withAdditionalKeyDeserializers(additional: KeyDeserializers?): DeserializerFactory? {
        return delegate.withAdditionalKeyDeserializers(additional)
    }

    override fun withDeserializerModifier(modifier: BeanDeserializerModifier?): DeserializerFactory? {
        return delegate.withDeserializerModifier(modifier)
    }

    override fun withAbstractTypeResolver(resolver: AbstractTypeResolver?): DeserializerFactory? {
        return delegate.withAbstractTypeResolver(resolver)
    }

    override fun withValueInstantiators(instantiators: ValueInstantiators?): DeserializerFactory? {
        return delegate.withValueInstantiators(instantiators)
    }

    override fun mapAbstractType(config: DeserializationConfig?, type: JavaType?): JavaType? {
        return delegate.mapAbstractType(config, type)
    }

    override fun findValueInstantiator(ctxt: DeserializationContext?, beanDesc: BeanDescription?): ValueInstantiator? {
        return delegate.findValueInstantiator(ctxt, beanDesc)
    }

    override fun createBuilderBasedDeserializer(
        ctxt: DeserializationContext?,
        type: JavaType?,
        beanDesc: BeanDescription?,
        builderClass: Class<*>?,
    ): JsonDeserializer<Any>? {
        return delegate.createBuilderBasedDeserializer(ctxt, type, beanDesc, builderClass)
    }

    override fun createEnumDeserializer(
        ctxt: DeserializationContext?,
        type: JavaType?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        return delegate.createEnumDeserializer(ctxt, type, beanDesc)
    }

    override fun createReferenceDeserializer(
        ctxt: DeserializationContext?,
        type: ReferenceType?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        return delegate.createReferenceDeserializer(ctxt, type, beanDesc)
    }

    override fun createTreeDeserializer(
        config: DeserializationConfig?,
        type: JavaType?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        return delegate.createTreeDeserializer(config, type, beanDesc)
    }

    override fun createArrayDeserializer(
        ctxt: DeserializationContext?,
        type: ArrayType?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        return delegate.createArrayDeserializer(ctxt, type, beanDesc)
    }

    override fun createCollectionDeserializer(
        ctxt: DeserializationContext,
        type: CollectionType,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        val deserializer = if (type.rawClass == KudosCollection::class.java) {
            val listType = ctxt.typeFactory.constructCollectionType(KudosList::class.java, type.contentType)
            delegate.createCollectionDeserializer(
                ctxt,
                listType,
                ctxt.config.introspect(listType),
            )
        } else {
            delegate.createCollectionDeserializer(ctxt, type, beanDesc)
        }

        if (deserializer is CollectionDeserializer && type.isTypeOrSubTypeOf(KudosCollection::class.java)) {
            return KudosCollectionDeserializer(deserializer, type.rawClass)
        }
        return deserializer
    }

    override fun createCollectionLikeDeserializer(
        ctxt: DeserializationContext?,
        type: CollectionLikeType?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        return delegate.createCollectionLikeDeserializer(ctxt, type, beanDesc)
    }

    override fun createMapDeserializer(
        ctxt: DeserializationContext?,
        type: MapType?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        return delegate.createMapDeserializer(ctxt, type, beanDesc)
    }

    override fun createMapLikeDeserializer(
        ctxt: DeserializationContext?,
        type: MapLikeType?,
        beanDesc: BeanDescription?,
    ): JsonDeserializer<*>? {
        return delegate.createMapLikeDeserializer(ctxt, type, beanDesc)
    }

    override fun createKeyDeserializer(ctxt: DeserializationContext?, type: JavaType?): KeyDeserializer? {
        return delegate.createKeyDeserializer(ctxt, type)
    }

    override fun findTypeDeserializer(config: DeserializationConfig?, baseType: JavaType?): TypeDeserializer? {
        return delegate.findTypeDeserializer(config, baseType)
    }

    override fun hasExplicitDeserializerFor(config: DeserializationConfig?, valueType: Class<*>?): Boolean {
        return delegate.hasExplicitDeserializerFor(config, valueType)
    }
}
