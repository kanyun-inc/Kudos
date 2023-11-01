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

import com.kanyun.kudos.compiler.KUDOS
import com.kanyun.kudos.compiler.KUDOS_FROM_JSON_FUNCTION_NAME
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.SimpleFunctionBuildingContext
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.javac.resolve.classId
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.runIf

class KudosFirDeclarationGenerator(session: FirSession) :
    FirDeclarationGenerationExtension(session) {

    companion object {
        private val PREDICATE = LookupPredicate.create {
            annotated(FqName(KUDOS))
        }
        private val kudosMethodsNames = setOf(Name.identifier(KUDOS_FROM_JSON_FUNCTION_NAME))
    }

    private val matchedClasses by lazy {
        session.predicateBasedProvider.getSymbolsByPredicate(PREDICATE)
            .filterIsInstance<FirRegularClassSymbol>()
    }

    private val key: GeneratedDeclarationKey
        get() = KudosPluginKey

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(PREDICATE)
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>): Set<Name> {
        if (classSymbol in matchedClasses) {
            return kudosMethodsNames
        }
        return super.getCallableNamesForClass(classSymbol)
    }

    @OptIn(SymbolInternals::class)
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        require(owner is FirRegularClassSymbol)
        if (callableId.callableName == Name.identifier(KUDOS_FROM_JSON_FUNCTION_NAME)) {
            val declaredFunctions =
                owner.declarationSymbols.filterIsInstance<FirNamedFunctionSymbol>()
            val function = runIf(declaredFunctions.none { it.isFromJson() }) {
                val thisTypeFir = owner.fir
                val returnType = ConeClassLikeTypeImpl(
                    ConeClassLikeLookupTagImpl(thisTypeFir.classId),
                    thisTypeFir.typeParameters.map {
                        it.toConeType()
                    }.toTypedArray(),
                    false,
                )
                createMemberFunctionForKudos(
                    owner,
                    callableId.callableName,
                    returnType,
                ) {
                    valueParameter(
                        Name.identifier("jsonReader"),
                        ClassId.fromString("android/util/JsonReader").createConeType(session),
                    )
                }
            }
            if (function != null) return listOf(function.symbol)
        }
        return super.generateFunctions(callableId, context)
    }

    private inline fun createMemberFunctionForKudos(
        owner: FirRegularClassSymbol,
        name: Name,
        returnType: ConeKotlinType,
        crossinline init: SimpleFunctionBuildingContext.() -> Unit = {},
    ): FirSimpleFunction {
        return createMemberFunction(owner, key, name, returnType) {
            modality = if (owner.modality == Modality.FINAL) Modality.FINAL else Modality.OPEN
            status { isOverride = true }
            init()
        }
    }

    private fun FirNamedFunctionSymbol.isFromJson(): Boolean {
        if (name != Name.identifier(KUDOS_FROM_JSON_FUNCTION_NAME)) return false
        val parameterSymbols = valueParameterSymbols
        if (parameterSymbols.size != 1) return false
        val jsonReaderSymbol = parameterSymbols[0]
        if (jsonReaderSymbol.resolvedReturnTypeRef.coneType.classId != ClassId.fromString("android/util/JsonReader")) return false
        return true
    }
}
