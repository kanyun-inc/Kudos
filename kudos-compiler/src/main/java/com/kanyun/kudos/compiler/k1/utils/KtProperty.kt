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

package com.kanyun.kudos.compiler.k1.utils

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext

/**
 * Created by Benny Huo on 2023/9/18
 */
fun KtProperty.initializedWithThisReference(bindingContext: BindingContext): Boolean {
    val initializer = this.initializer ?: return false
    val propertyDescriptor =
        bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, this] as? PropertyDescriptor ?: return false
    val thisReference = propertyDescriptor.dispatchReceiverParameter
    var result = false
    initializer.accept(object : KtVisitorVoid() {
        override fun visitKtElement(element: KtElement) {
            super.visitKtElement(element)
            if (!result) element.acceptChildren(this)
        }

        override fun visitThisExpression(expression: KtThisExpression) {
            super.visitThisExpression(expression)
            result = true
        }

        override fun visitReferenceExpression(expression: KtReferenceExpression) {
            super.visitReferenceExpression(expression)
            when (val descriptor = bindingContext[BindingContext.REFERENCE_TARGET, expression]) {
                is CallableMemberDescriptor -> {
                    result = thisReference == descriptor.dispatchReceiverParameter
                }
            }
        }
    })
    return result
}
