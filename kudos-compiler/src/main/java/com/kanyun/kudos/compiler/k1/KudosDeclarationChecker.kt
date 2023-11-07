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

import com.kanyun.kudos.compiler.KudosNames.CONTAINER_FQ_NAMES
import com.kanyun.kudos.compiler.KudosNames.JSON_ADAPTER_NAME
import com.kanyun.kudos.compiler.KudosNames.KUDOS_IGNORE
import com.kanyun.kudos.compiler.KudosNames.KUDOS_NAME
import com.kanyun.kudos.compiler.KudosNames.TRANSIENT
import com.kanyun.kudos.compiler.k1.diagnostic.KudosErrors
import com.kanyun.kudos.compiler.k1.utils.initializedWithThisReference
import com.kanyun.kudos.compiler.k1.utils.isLazyCall
import com.kanyun.kudos.compiler.options.Options
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.getKotlinTypeFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.representativeUpperBound

class KudosDeclarationChecker(
    private val noArgAnnotations: List<String>,
) : DeclarationChecker {

    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext,
    ) {
        if (
            declaration is KtClass &&
            descriptor is ClassDescriptor &&
            descriptor.annotations.hasAnnotation(KUDOS_NAME)
        ) {
            if (declaration.typeParameters.isNotEmpty()) {
                context.trace.report(KudosErrors.GENERIC_TYPE.on(declaration))
            } else {
                checkInitBlock(context, declaration)
                checkNoArg(descriptor, context, declaration)
                if (Options.gson()) {
                    checkJsonAdapter(descriptor, context, declaration)
                }
                checkPrimaryConstructor(declaration, context)
                checkProperties(declaration, context)
            }
        }
    }

    private fun checkInitBlock(
        context: DeclarationCheckerContext,
        declaration: KtClass,
    ) {
        if (declaration.getAnonymousInitializers().isNotEmpty()) {
            context.trace.report(KudosErrors.INIT_BLOCK.on(declaration))
        }
    }

    private fun checkNoArg(
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext,
        declaration: KtDeclaration,
    ) {
        val noArgAnnotation = noArgAnnotations.firstOrNull { descriptor.annotations.hasAnnotation(FqName(it)) }
        if (noArgAnnotation != null) {
            context.trace.report(KudosErrors.CONFLICTS_WITH_NOARG.on(declaration, noArgAnnotation))
        }
    }

    private fun checkJsonAdapter(
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext,
        declaration: KtDeclaration,
    ) {
        if (descriptor.annotations.hasAnnotation(JSON_ADAPTER_NAME)) {
            context.trace.report(KudosErrors.CONFLICTS_WITH_JSON_ADAPTER.on(declaration))
        }
    }

    private fun checkPrimaryConstructor(
        declaration: KtClass,
        context: DeclarationCheckerContext,
    ) {
        declaration.primaryConstructor?.valueParameters?.forEach {
            if (it.isVarArg) {
                context.trace.report(KudosErrors.VARARG_PARAMETER.on(declaration))
            }

            if (!it.hasValOrVar()) {
                context.trace.report(KudosErrors.NOT_PROPERTY_PARAMETER.on(declaration))
            }
        }
    }

    private fun checkProperties(
        declaration: KtClass,
        context: DeclarationCheckerContext,
    ) {
        for (property in declaration.getProperties()) {
            checkPropertyInitializer(context, property)
            if (property.fieldDeclaration != null && !property.hasDelegate()) {
                checkPropertyType(context, property)
            }
        }

        for (property in declaration.primaryConstructorParameters) {
            if (property.hasValOrVar()) {
                checkPropertyType(context, property)
            }
        }
    }

    private fun checkPropertyInitializer(
        context: DeclarationCheckerContext,
        property: KtProperty,
    ) {
        if (property.hasDelegate() && property.delegateExpression?.isLazyCall(context.trace.bindingContext) != true) {
            context.trace.report(KudosErrors.PROPERTY_DELEGATE.on(property, property.name ?: "???"))
        } else if (property.initializedWithThisReference(context.trace.bindingContext)) {
            context.trace.report(KudosErrors.PROPERTY_INITIALIZER.on(property, property.name ?: "???"))
        }
    }

    private fun checkPropertyType(
        context: DeclarationCheckerContext,
        property: KtCallableDeclaration,
    ) {
        if (
            property.annotationEntries.any {
                val annotationName = context.trace.bindingContext[BindingContext.ANNOTATION, it]?.fqName?.asString()
                annotationName == KUDOS_IGNORE || annotationName == TRANSIENT
            }
        ) {
            return
        }

        val propertyType = context.trace.bindingContext[BindingContext.TYPE, property.typeReference] ?: return
        for (type in findNonKudosType(propertyType)) {
            context.trace.report(
                KudosErrors.NON_KUDOS_PROPERTY_TYPE.on(
                    property.typeReference!!,
                    type.getKotlinTypeFqName(false),
                ),
            )
        }
    }

    private fun findNonKudosType(type: KotlinType): List<KotlinType> {
        if (
            KotlinBuiltIns.isPrimitiveTypeOrNullablePrimitiveType(type) ||
            KotlinBuiltIns.isPrimitiveArray(type) ||
            KotlinBuiltIns.isStringOrNullableString(type) ||
            KotlinBuiltIns.isUnsignedArrayType(type)
        ) {
            return emptyList()
        }

        if (type.hasAnnotation(KUDOS_NAME)) {
            return emptyList()
        }

        val fqName = type.getErasedUpperBound().getKotlinTypeFqName(false)
        if (fqName in CONTAINER_FQ_NAMES) {
            return type.arguments.flatMap { findNonKudosType(it.type) }
        }
        return listOf(type)
    }

    private fun KotlinType.getErasedUpperBound(): KotlinType =
        (constructor.declarationDescriptor as? TypeParameterDescriptor)?.representativeUpperBound?.getErasedUpperBound()
            ?: this

    private fun KotlinType.hasAnnotation(fqName: FqName): Boolean {
        return constructor.declarationDescriptor?.annotations?.hasAnnotation(fqName) ?: false
    }
}
