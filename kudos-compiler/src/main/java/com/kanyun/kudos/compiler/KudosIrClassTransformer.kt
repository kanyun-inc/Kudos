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

package com.kanyun.kudos.compiler

import com.kanyun.kudos.compiler.options.Options
import com.kanyun.kudos.compiler.utils.addOverride
import com.kanyun.kudos.compiler.utils.hasKudosAnnotation
import com.kanyun.kudos.compiler.utils.irThis
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.superClass
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.JvmNames
import org.jetbrains.kotlin.name.Name

/**
 * Created by Benny Huo on 2023/9/8
 */
class KudosIrClassTransformer(
    private val context: IrPluginContext,
    private val irClass: IrClass,
    private val noArgConstructors: MutableMap<IrClass, IrConstructor>,
) {

    private val defaults = HashSet<String>()

    fun transform() {
        if (Options.isGsonEnabled()) {
            generateJsonAdapter()
        }
        generateNoArgConstructor()
        generateValidator()
    }

    private fun generateJsonAdapter() {
        val jsonAdapter = context.referenceConstructors(ClassId.topLevel(FqName(JSON_ADAPTER))).firstOrNull()
            ?: throw IllegalStateException(
                "Constructors of class $JSON_ADAPTER not found while isGsonEnabled is set to true. " +
                    "Please check your dependencies to ensure the existing of the Gson library.",
            )
        irClass.annotations += IrConstructorCallImpl.fromSymbolOwner(
            jsonAdapter.owner.returnType,
            jsonAdapter.owner.symbol,
        ).apply {
            val adapterFactory = context.referenceClass(ClassId.topLevel(FqName(ADAPTER_FACTORY)))!!

            putValueArgument(
                0,
                IrClassReferenceImpl(
                    startOffset,
                    endOffset,
                    context.irBuiltIns.kClassClass.starProjectedType,
                    context.irBuiltIns.kClassClass,
                    adapterFactory.defaultType,
                ),
            )
        }
    }

    private fun generateNoArgConstructor() {
        if (needsNoargConstructor(irClass)) {
            irClass.declarations.add(getOrGenerateNoArgConstructor(irClass))
        }
    }

    private fun getOrGenerateNoArgConstructor(klass: IrClass): IrConstructor = noArgConstructors.getOrPut(klass) {
        val superClass = klass.superTypes.mapNotNull(IrType::getClass).singleOrNull { it.kind == ClassKind.CLASS }
            ?: context.irBuiltIns.anyClass.owner

        val superConstructor = if (needsNoargConstructor(superClass)) {
            getOrGenerateNoArgConstructor(superClass)
        } else {
            superClass.constructors.singleOrNull {
                it.isZeroParameterConstructor()
            } ?: error(
                "No noarg super constructor for ${klass.render()}:\n" +
                    superClass.constructors.joinToString("\n") { it.render() },
            )
        }

        context.irFactory.buildConstructor {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            returnType = klass.defaultType
        }.also { ctor ->
            ctor.parent = klass

            val builder = object : IrBuilderWithScope(context, Scope(ctor.symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET) {
                fun setupDefaultValues(): List<IrStatement> {
                    return klass.primaryConstructor?.valueParameters?.filter {
                        it.defaultValue != null
                    }?.mapNotNull { parameter ->
                        klass.properties.find {
                            it.name == parameter.name
                        }?.backingField?.let { field ->
                            val init = field.initializer?.expression as? IrGetValue
                            if (init?.origin == IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER) {
                                defaults += field.name.asString()

                                irSetField(
                                    IrGetValueImpl(
                                        startOffset,
                                        endOffset,
                                        klass.thisReceiver!!.symbol,
                                    ),
                                    field,
                                    parameter.defaultValue!!.expression,
                                )
                            } else {
                                null
                            }
                        }
                    } ?: emptyList()
                }
            }

            ctor.body = context.irFactory.createBlockBody(
                ctor.startOffset,
                ctor.endOffset,
                listOfNotNull(
                    // call super
                    IrDelegatingConstructorCallImpl(
                        ctor.startOffset,
                        ctor.endOffset,
                        context.irBuiltIns.unitType,
                        superConstructor.symbol,
                        0,
                        superConstructor.valueParameters.size,
                    ),
                    // call init blocks
                    IrInstanceInitializerCallImpl(
                        ctor.startOffset,
                        ctor.endOffset,
                        klass.symbol,
                        context.irBuiltIns.unitType,
                    ),
                ) + builder.setupDefaultValues(),
            )
        }
    }

    private fun generateValidator() {
        val nonDefaults = ArrayList<String>()
        val collections = ArrayList<IrField>()
        val arrays = ArrayList<IrField>()

        irClass.properties.forEach { property ->
            if (property.isDelegated) return@forEach
            val backingField = property.backingField ?: return@forEach
            val fieldName = backingField.name.asString()

            // do not check property from body. always initialized properly.
            if ((backingField.initializer?.expression as? IrGetValue)?.origin == IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER) {
                if (!backingField.type.isNullable() && fieldName !in defaults) {
                    nonDefaults += fieldName
                }
            }

            val type = backingField.type
            if (type is IrSimpleType) {
                // For container types, the only first type argument is the element type.
                if (type.arguments.firstOrNull()?.typeOrNull?.isNullable() == false) {
                    if (type.isSubtypeOfClass(context.irBuiltIns.arrayClass)) {
                        arrays += backingField
                    } else if (type.isSubtypeOfClass(context.irBuiltIns.collectionClass)) {
                        collections += backingField
                    }
                }
            }
        }

        if (nonDefaults.isEmpty() && collections.isEmpty() && arrays.isEmpty()) return

        val kudosValidator = FqName(KUDOS_VALIDATOR)
        val statusType = context.irBuiltIns.mapClass.typeWith(
            context.irBuiltIns.stringType,
            context.irBuiltIns.booleanType,
        )

        val validateFunction = irClass.functions.find {
            it.name.asString() == "validate" && it.valueParameters.singleOrNull {
                it.type == statusType
            } != null
        }

        if (validateFunction?.isFakeOverride == false) {
            return
        } else if (validateFunction?.isFakeOverride == true) {
            irClass.declarations.remove(validateFunction)
        }

        irClass.addOverride(kudosValidator, "validate", context.irBuiltIns.unitType, Modality.OPEN).apply {
            dispatchReceiverParameter = irClass.thisReceiver!!.copyTo(this)
            val statusParameter = addValueParameter {
                name = Name.identifier("status")
                type = statusType
            }

            val validateField = context.referenceFunctions(
                CallableId(FqName("com.kanyun.kudos.validator"), Name.identifier("validateField")),
            ).first()

            val validateCollection = context.referenceFunctions(
                CallableId(FqName("com.kanyun.kudos.validator"), Name.identifier("validateCollection")),
            ).first()

            val validateArray = context.referenceFunctions(
                CallableId(FqName("com.kanyun.kudos.validator"), Name.identifier("validateArray")),
            ).first()

            body = IrBlockBodyBuilder(context, Scope(symbol), SYNTHETIC_OFFSET, SYNTHETIC_OFFSET).blockBody {
                val status = irGet(statusParameter.type, statusParameter.symbol)

                // region call super
                val superClass = irClass.superClass
                if (superClass != null) {
                    overriddenSymbols.firstOrNull {
                        it.owner.parentClassOrNull == superClass
                    }?.let {
                        +irCall(this@apply, superQualifierSymbol = superClass.symbol).also { call ->
                            call.dispatchReceiver = irThis()
                            call.putValueArgument(0, status)
                        }
                    }
                }
                // endregion

                nonDefaults.forEach { fieldName ->
                    +irCall(validateField).apply {
                        putValueArgument(
                            0,
                            IrConstImpl.string(
                                SYNTHETIC_OFFSET,
                                SYNTHETIC_OFFSET,
                                context.irBuiltIns.stringType,
                                fieldName,
                            ),
                        )
                        putValueArgument(1, status)
                    }
                }

                collections.forEach { field ->
                    +irCall(validateCollection).apply {
                        putValueArgument(
                            0,
                            IrConstImpl.string(
                                SYNTHETIC_OFFSET,
                                SYNTHETIC_OFFSET,
                                context.irBuiltIns.stringType,
                                field.name.asString(),
                            ),
                        )
                        putValueArgument(1, irGetField(irThis(), field))

                        field.type.classFqName?.shortNameOrSpecial()?.asString()?.let { typeName ->
                            putValueArgument(
                                2,
                                IrConstImpl.string(
                                    SYNTHETIC_OFFSET,
                                    SYNTHETIC_OFFSET,
                                    context.irBuiltIns.stringType,
                                    typeName,
                                ),
                            )
                        }
                    }
                }

                arrays.forEach { field ->
                    +irCall(validateArray).apply {
                        putValueArgument(
                            0,
                            IrConstImpl.string(
                                SYNTHETIC_OFFSET,
                                SYNTHETIC_OFFSET,
                                context.irBuiltIns.stringType,
                                field.name.asString(),
                            ),
                        )
                        putValueArgument(
                            1,
                            irGetField(irThis(), field),
                        )
                    }
                }
            }
        }
    }

    private fun needsNoargConstructor(declaration: IrClass): Boolean =
        declaration.kind == ClassKind.CLASS &&
            declaration.hasKudosAnnotation() &&
            declaration.constructors.none { it.isZeroParameterConstructor() }

    // Returns true if this constructor is callable with no arguments by JVM rules, i.e. will have descriptor `()V`.
    private fun IrConstructor.isZeroParameterConstructor(): Boolean {
        return valueParameters.all {
            it.defaultValue != null
        } && (valueParameters.isEmpty() || isPrimary || hasAnnotation(JvmNames.JVM_OVERLOADS_FQ_NAME))
    }
}
