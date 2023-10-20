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

package com.kanyun.kudos.compiler.k1.symbol

import com.kanyun.kudos.compiler.KUDOS_FROM_JSON_FUNCTION_NAME
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name

class FromJsonFunctionDescriptorImpl(
    private val classDescriptor: ClassDescriptor,
) : SimpleFunctionDescriptorImpl(
    classDescriptor,
    null,
    Annotations.EMPTY,
    Name.identifier(KUDOS_FROM_JSON_FUNCTION_NAME),
    CallableMemberDescriptor.Kind.SYNTHESIZED,
    classDescriptor.source,
) {
    fun initialize(
        valueParameters: List<ValueParameterDescriptor> = emptyList(),
    ) {
        super.initialize(
            null,
            classDescriptor.thisAsReceiverParameter,
            emptyList(),
            valueParameters,
            classDescriptor.defaultType,
            Modality.FINAL,
            DescriptorVisibilities.PUBLIC,
        )
    }
}
