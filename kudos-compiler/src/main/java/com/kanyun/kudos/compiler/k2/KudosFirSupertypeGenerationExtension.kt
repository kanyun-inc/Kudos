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

import com.kanyun.kudos.compiler.KudosNames.KUDOS_ANNOTATION_CLASS_ID
import com.kanyun.kudos.compiler.KudosNames.KUDOS_JSON_ADAPTER_CLASS_ID
import com.kanyun.kudos.compiler.KudosNames.KUDOS_NAME
import com.kanyun.kudos.compiler.KudosNames.KUDOS_VALIDATOR_CLASS_ID
import com.kanyun.kudos.compiler.options.Options
import com.kanyun.kudos.compiler.utils.safeAs
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirTypeAlias
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.references.impl.FirSimpleNamedReference
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirUserTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.toSymbol
import org.jetbrains.kotlin.name.ClassId

/**
 * Created by benny at 2023/5/29 14:47.
 */
class KudosFirSupertypeGenerationExtension(
    session: FirSession,
    private val kudosAnnotationValueMap: HashMap<String, List<Int>>,
) : FirSupertypeGenerationExtension(session) {

    private val hasKudos = DeclarationPredicate.create {
        annotated(KUDOS_NAME)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(hasKudos)
    }

    context(TypeResolveServiceContainer)
    override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>,
    ): List<FirResolvedTypeRef> {
        var hasValidator = false
        var hasJsonAdapter = false
        val annotationValues = classLikeDeclaration.symbol.resolvedAnnotationsWithArguments.getAnnotationByClassId(KUDOS_ANNOTATION_CLASS_ID, session)
            ?.getIntArrayArgument()
        kudosAnnotationValueMap[classLikeDeclaration.classId.toString()] = annotationValues ?: emptyList()
        for (superTypeRef in resolvedSupertypes) {
            val superType = superTypeRef.type
            val superTypeClassIds = superType.allSuperTypeClassIds()
            if (KUDOS_VALIDATOR_CLASS_ID in superTypeClassIds) {
                hasValidator = true
            }
            if (KUDOS_JSON_ADAPTER_CLASS_ID in superTypeClassIds) {
                hasJsonAdapter = true
            }
        }

        val firTypeRefList = mutableListOf<FirResolvedTypeRef>()
        if (!hasValidator) {
            firTypeRefList += buildResolvedTypeRef {
                type = KUDOS_VALIDATOR_CLASS_ID.constructClassLikeType(
                    emptyArray(),
                    isNullable = false,
                )
            }
        }
        if (Options.isAndroidJsonReaderEnabled(kudosAnnotationValueMap, classLikeDeclaration.classId.toString())) {
            if (!hasJsonAdapter) {
                val genericType = ConeClassLikeTypeImpl(
                    ConeClassLikeLookupTagImpl(classLikeDeclaration.classId),
                    classLikeDeclaration.typeParameters.map {
                        it.toConeType()
                    }.toTypedArray(),
                    false,
                )
                firTypeRefList += buildResolvedTypeRef {
                    type = KUDOS_JSON_ADAPTER_CLASS_ID.constructClassLikeType(
                        arrayOf(genericType),
                        isNullable = false,
                    )
                }
            }
        }
        return firTypeRefList
    }

    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean {
        return declaration is FirClass &&
            declaration.origin == FirDeclarationOrigin.Source &&
            session.predicateBasedProvider.matches(hasKudos, declaration)
    }

    context(TypeResolveServiceContainer)
    @OptIn(SymbolInternals::class)
    private fun ConeKotlinType.allSuperTypeClassIds(): List<ClassId> {
        if (this !is ConeClassLikeType) return emptyList()

        val thisTypeSession = toSymbol(session)?.moduleData?.session ?: return emptyList()
        val thisTypeFir = lookupTag.toSymbol(thisTypeSession)?.fir ?: return emptyList()
        return listOf(thisTypeFir.classId) + thisTypeFir.superTypes().flatMap {
            it.allSuperTypeClassIds()
        }
    }

    context(TypeResolveServiceContainer)
    private fun FirClassLikeDeclaration.superTypes() = when (this) {
        is FirRegularClass -> superTypeRefs
        is FirTypeAlias -> listOf(expandedTypeRef)
        else -> emptyList()
    }.mapNotNull {
        when (it) {
            is FirUserTypeRef -> typeResolver.resolveUserType(it).type
            is FirResolvedTypeRef -> it.type
            else -> null
        }
    }

    private fun FirAnnotation.getIntArrayArgument(): List<Int>? {
        if (this !is FirAnnotationCall) return null
        return arguments.map {
            when (val annotationValue = it.safeAs<FirPropertyAccessExpression>()?.calleeReference?.safeAs<FirSimpleNamedReference>()?.name?.asString()) {
                "KUDOS_ANDROID_JSON_READER" -> {
                    Options.KUDOS_ANDROID_JSON_READER
                }

                "KUDOS_GSON" -> {
                    Options.KUDOS_GSON
                }

                "KUDOS_JACKSON" -> {
                    Options.KUDOS_JACKSON
                }

                else -> {
                    throw IllegalArgumentException("unknown annotation argument $annotationValue")
                }
            }
        }
    }
}
