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

package com.kanyun.kudos.compiler.k1

import com.kanyun.kudos.compiler.KUDOS
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.typeUtil.supertypes

class KudosSyntheticResolveExtension : SyntheticResolveExtension {

    override fun addSyntheticSupertypes(thisDescriptor: ClassDescriptor, supertypes: MutableList<KotlinType>) {
        if (thisDescriptor.kind != ClassKind.CLASS) return
        if (thisDescriptor.annotations.hasAnnotation(FqName(KUDOS))) {
            val superTypeNames = supertypes.asSequence().flatMap {
                listOf(it) + it.supertypes()
            }.map {
                it.getKotlinTypeFqName(false)
            }.toSet()

            if ("com.kanyun.kudos.validator.KudosValidator" in superTypeNames) return

            val kudosValidator = thisDescriptor.module.findClassAcrossModuleDependencies(
                ClassId(
                    FqName("com.kanyun.kudos.validator"),
                    Name.identifier("KudosValidator"),
                ),
            )!!

            supertypes.add(
                KotlinTypeFactory.simpleNotNullType(
                    TypeAttributes.Empty,
                    kudosValidator,
                    emptyList(),
                ),
            )
        }
    }
}
