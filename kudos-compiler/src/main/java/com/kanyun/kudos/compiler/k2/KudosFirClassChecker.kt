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

package com.kanyun.kudos.compiler.k2

import com.kanyun.kudos.compiler.KudosNames.CONTAINER_FQ_NAMES
import com.kanyun.kudos.compiler.KudosNames.JSON_ADAPTER_NAME
import com.kanyun.kudos.compiler.KudosNames.KUDOS_IGNORE_NAME
import com.kanyun.kudos.compiler.KudosNames.KUDOS_NAME
import com.kanyun.kudos.compiler.KudosNames.TRANSIENT_NAME
import com.kanyun.kudos.compiler.k2.diagnostic.KudosKtErrors
import com.kanyun.kudos.compiler.k2.utils.initializedWithThisReference
import com.kanyun.kudos.compiler.k2.utils.isLazyCall
import com.kanyun.kudos.compiler.options.Options
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.hasValOrVar
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.unsubstitutedScope
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.declarations.utils.anonymousInitializers
import org.jetbrains.kotlin.fir.declarations.utils.fromPrimaryConstructor
import org.jetbrains.kotlin.fir.declarations.utils.hasBackingField
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.scopes.processAllProperties
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isPrimitiveArray
import org.jetbrains.kotlin.fir.types.isPrimitiveOrNullablePrimitive
import org.jetbrains.kotlin.fir.types.isUnsignedTypeOrNullableUnsignedType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.toSymbol
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.StandardClassIds

/**
 * Created by Benny Huo on 2023/8/21
 */
@OptIn(SymbolInternals::class)
class KudosFirClassChecker(
    private val noArgAnnotations: List<String>,
) : FirClassChecker() {
    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        if (declaration.hasAnnotation(ClassId.topLevel(KUDOS_NAME), context.session)) {
            if (declaration.typeParameters.isNotEmpty()) {
                reporter.reportOn(declaration.source, KudosKtErrors.GENERIC_TYPE, context)
            } else {
                checkInitBlock(declaration, context, reporter)
                checkNoArg(declaration, context, reporter)
                if (Options.gson()) {
                    checkJsonAdapter(declaration, context, reporter)
                }
                checkPrimaryConstructor(declaration, context, reporter)
                checkProperties(declaration, context, reporter)
            }
        }
    }

    private fun checkInitBlock(
        declaration: FirClass,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        if (declaration.anonymousInitializers.isNotEmpty()) {
            reporter.reportOn(declaration.source, KudosKtErrors.INIT_BLOCK, context)
        }
    }

    private fun checkNoArg(
        declaration: FirClass,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val noArgAnnotation = noArgAnnotations.firstOrNull {
            declaration.hasAnnotation(ClassId.topLevel(FqName(it)), context.session)
        }
        if (noArgAnnotation != null) {
            reporter.reportOn(declaration.source, KudosKtErrors.CONFLICTS_WITH_NOARG, noArgAnnotation, context)
        }
    }

    private fun checkJsonAdapter(
        declaration: FirClass,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        if (declaration.hasAnnotation(ClassId.topLevel(JSON_ADAPTER_NAME), context.session)) {
            reporter.reportOn(declaration.source, KudosKtErrors.CONFLICTS_WITH_JSON_ADAPTER, context)
        }
    }

    private fun checkPrimaryConstructor(
        declaration: FirClass,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        declaration.primaryConstructorIfAny(context.session)?.valueParameterSymbols?.forEach {
            if (it.isVararg) {
                reporter.reportOn(declaration.source, KudosKtErrors.VARARG_PARAMETER, context)
            }

            if (it.source?.hasValOrVar() != true) {
                reporter.reportOn(declaration.source, KudosKtErrors.NOT_PROPERTY_PARAMETER, context)
            }
        }
    }

    private fun checkProperties(
        declaration: FirClass,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        declaration.unsubstitutedScope(context).processAllProperties {
            if (it is FirPropertySymbol) {
                checkPropertyInitializer(it, context, reporter)
                checkPropertyType(it, context, reporter)
            }
        }
    }

    private fun checkPropertyInitializer(
        symbol: FirPropertySymbol,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        if (symbol.fromPrimaryConstructor) return
        if (symbol.hasDelegate && symbol.fir.delegate?.isLazyCall() == false) {
            reporter.reportOn(
                symbol.source,
                KudosKtErrors.PROPERTY_DELEGATE,
                symbol.name.asString(),
                context,
            )
        } else if (symbol.initializedWithThisReference()) {
            reporter.reportOn(
                symbol.source,
                KudosKtErrors.PROPERTY_INITIALIZER,
                symbol.name.asString(),
                context,
            )
        }
    }

    private fun checkPropertyType(
        symbol: FirPropertySymbol,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        if (symbol.hasBackingField && !symbol.hasDelegate &&
            !symbol.hasAnnotation(ClassId.topLevel(KUDOS_IGNORE_NAME), context.session) &&
            symbol.backingFieldSymbol?.hasAnnotation(ClassId.topLevel(TRANSIENT_NAME), context.session) != true
        ) {
            for (type in findNonKudosType(symbol.resolvedReturnType, context)) {
                reporter.reportOn(
                    symbol.fir.returnTypeRef.source,
                    KudosKtErrors.NON_KUDOS_PROPERTY_TYPE,
                    type.fqName(context.session) ?: type.renderReadable(),
                    context,
                )
            }
        }
    }

    private fun findNonKudosType(type: ConeKotlinType, context: CheckerContext): List<ConeKotlinType> {
        if (
            type.isPrimitiveOrNullablePrimitive ||
            type.isPrimitiveArray ||
            type.isStringOrNullableString ||
            type.isUnsignedTypeOrNullableUnsignedType ||
            type.isUnsignedPrimitiveArray
        ) {
            return emptyList()
        }

        if (type is ConeClassLikeType) {
            val fqName = type.getErasedUpperBound(context.session)?.fqName(context.session) ?: return listOf(type)
            if (fqName in CONTAINER_FQ_NAMES) {
                return type.typeArguments.flatMap {
                    it.type?.let { type -> findNonKudosType(type, context) } ?: emptyList()
                }
            }
        }

        val annotations = type.toRegularClassSymbol(context.session)?.annotations
        if (annotations != null) {
            for (annotation in annotations) {
                val fqName = annotation.fqName(context.session) ?: continue
                if (fqName.asString() == KUDOS_NAME.asString()) {
                    return emptyList()
                }
            }
        }

        return listOf(type)
    }

    private val ConeKotlinType.isUnsignedPrimitiveArray: Boolean
        get() = this is ConeClassLikeType && lookupTag.classId in StandardClassIds.unsignedArrayTypeByElementType.values

    private fun ConeKotlinType.fqName(session: FirSession): String? {
        if (this is ConeClassLikeType) {
            return toSymbol(session)?.classId?.asFqNameString()
        }
        return null
    }

    private val ConeKotlinType.isStringOrNullableString: Boolean
        get() {
            if (this !is ConeClassLikeType) return false
            return lookupTag.classId == StandardClassIds.String
        }

    private fun ConeKotlinType.getErasedUpperBound(session: FirSession): ConeClassLikeType? =
        when (this) {
            is ConeClassLikeType ->
                fullyExpandedType(session)

            is ConeTypeParameterType -> {
                val bounds = lookupTag.typeParameterSymbol.resolvedBounds
                val representativeBound = bounds.firstOrNull {
                    val kind = it.coneType.toRegularClassSymbol(session)?.classKind
                        ?: return@firstOrNull false
                    kind != ClassKind.INTERFACE && kind != ClassKind.ANNOTATION_CLASS
                } ?: bounds.first()
                representativeBound.coneType.getErasedUpperBound(session)
            }

            else -> null
        }
}
