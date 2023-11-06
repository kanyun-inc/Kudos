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

import com.kanyun.kudos.compiler.KudosNames.KUDOS_JSON_ADAPTER_CLASS_ID
import com.kanyun.kudos.compiler.utils.irThis
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBranch
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irElseBranch
import org.jetbrains.kotlin.ir.builders.irEquals
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.builders.irWhen
import org.jetbrains.kotlin.ir.builders.irWhile
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class KudosFromJsonFunctionBuilder(
    private val irClass: IrClass,
    private val irFunction: IrFunction,
    private val pluginContext: IrPluginContext,
    startOffset: Int = SYNTHETIC_OFFSET,
    endOffset: Int = SYNTHETIC_OFFSET,
) : IrBlockBodyBuilder(pluginContext, Scope(irFunction.symbol), startOffset, endOffset) {

    init {
        irFunction.body = doBuild()
    }

    private val jsonReader = irFunction.valueParameters.first()

    fun generateBody(): KudosFromJsonFunctionBuilder {
        val fields = ArrayList<IrField>()
        irClass.properties.forEach { property ->
            if (property.isDelegated) return@forEach
            val backingField = property.backingField ?: return@forEach
            fields.add(backingField)
        }
        +irCall(
            pluginContext.referenceFunctions(
                CallableId(FqName("android.util"), FqName("JsonReader"), Name.identifier("beginObject")),
            ).first(),
        ).apply {
            dispatchReceiver = irGet(jsonReader)
        }
        +irWhile().apply {
            condition = irCall(
                pluginContext.referenceFunctions(
                    CallableId(FqName("android.util"), FqName("JsonReader"), Name.identifier("hasNext")),
                ).first(),
            ).apply {
                dispatchReceiver = irGet(jsonReader)
            }
            body = irBlock {
                val name = irTemporary(
                    irCall(
                        pluginContext.referenceFunctions(
                            CallableId(FqName("android.util"), FqName("JsonReader"), Name.identifier("nextName")),
                        ).first(),
                    ).apply {
                        dispatchReceiver = irGet(jsonReader)
                    },
                )
                val branches = ArrayList<IrBranch>()
                fields.forEach { field ->
                    branches.add(
                        irBranch(
                            irEquals(irGet(name), irString(field.name.asString())),
                            irSetField(irFunction.irThis(), field, getNextValue(field)),
                        ),
                    )
                }
                branches.add(
                    irElseBranch(
                        irCall(
                            pluginContext.referenceFunctions(
                                CallableId(FqName("android.util"), FqName("JsonReader"), Name.identifier("skipValue")),
                            ).first(),
                        ).apply {
                            dispatchReceiver = irGet(jsonReader)
                        },
                    ),
                )
                +irWhen(context.irBuiltIns.unitType, branches)
            }
        }
        +irCall(
            pluginContext.referenceFunctions(
                CallableId(FqName("android.util"), FqName("JsonReader"), Name.identifier("endObject")),
            ).first(),
        ).apply {
            dispatchReceiver = irGet(jsonReader)
        }
        +irReturn(
            irFunction.irThis(),
        )
        return this
    }

    private fun getJsonReaderNextSymbol(type: String): IrSimpleFunctionSymbol {
        return pluginContext.referenceFunctions(
            CallableId(
                FqName("android.util"),
                FqName("JsonReader"),
                Name.identifier("next$type"),
            ),
        ).first()
    }

    private fun getNextValue(field: IrField): IrExpression {
        return if (field.type.isSubtypeOfClass(context.irBuiltIns.stringClass)) {
            irCall(getJsonReaderNextSymbol("String")).apply {
                dispatchReceiver = irGet(jsonReader)
            }
        } else if (field.type.isSubtypeOfClass(context.irBuiltIns.longClass)) {
            irCall(getJsonReaderNextSymbol("Long")).apply {
                dispatchReceiver = irGet(jsonReader)
            }
        } else if (field.type.isSubtypeOfClass(context.irBuiltIns.intClass)) {
            irCall(getJsonReaderNextSymbol("Int")).apply {
                dispatchReceiver = irGet(jsonReader)
            }
        } else if (field.type.isSubtypeOfClass(context.irBuiltIns.doubleClass)) {
            irCall(getJsonReaderNextSymbol("Double")).apply {
                dispatchReceiver = irGet(jsonReader)
            }
        } else if (field.type.isSubtypeOfClass(context.irBuiltIns.floatClass)) {
            irCall(
                pluginContext.referenceFunctions(
                    CallableId(FqName("kotlin.text"), Name.identifier("toFloat")),
                ).first().owner,
            ).apply {
                extensionReceiver = irCall(getJsonReaderNextSymbol("String")).apply {
                    dispatchReceiver = irGet(jsonReader)
                }
            }
        } else if (field.type.isSubtypeOfClass(context.irBuiltIns.booleanClass)) {
            irCall(getJsonReaderNextSymbol("Boolean")).apply {
                dispatchReceiver = irGet(jsonReader)
            }
        } else if (
            field.type.isSubtypeOfClass(context.irBuiltIns.listClass) ||
            field.type.isSubtypeOfClass(context.irBuiltIns.arrayClass) ||
            field.type.isSubtypeOfClass(context.irBuiltIns.setClass) ||
            field.type.isSubtypeOfClass(context.irBuiltIns.mapClass) ||
            field.type.isSubtypeOfClass(
                pluginContext.referenceClass(KUDOS_JSON_ADAPTER_CLASS_ID)!!,
            )
        ) {
            irCall(
                pluginContext.referenceFunctions(
                    CallableId(FqName("com.kanyun.kudos.json.reader.adapter"), Name.identifier("parseKudosObject")),
                ).first(),
            ).apply {
                putValueArgument(0, irGet(jsonReader))
                putValueArgument(1, getParameterizedType(field.type))
            }
        } else {
            throw Exception("Kudos UnSupported type")
        }
    }

    private fun getParameterizedType(type: IrType): IrExpression {
        var typeArguments = listOf<IrTypeProjection>()
        if (type is IrSimpleType) {
            typeArguments = type.arguments.filterIsInstance<IrTypeProjection>()
        }
        if (typeArguments.isEmpty()) {
            return irCall(
                pluginContext.referenceProperties(
                    CallableId(FqName("kotlin.jvm"), Name.identifier("javaObjectType")),
                ).first().owner.getter!!,
            ).apply {
                extensionReceiver = kClassReference(type)
            }
        }
        val irVararg = irVararg(
            pluginContext.referenceClass(ClassId(FqName("java.lang.reflect"), Name.identifier("Type")))!!.defaultType,
            typeArguments.map { getParameterizedType(it.type) },
        )
        val typeArray = irCall(
            pluginContext.referenceFunctions(
                CallableId(FqName("kotlin"), Name.identifier("arrayOf")),
            ).first(),
        ).apply {
            putValueArgument(0, irVararg)
        }
        return irCall(
            pluginContext.referenceClass(
                ClassId(
                    FqName("com.kanyun.kudos.json.reader.adapter"),
                    Name.identifier("ParameterizedTypeImpl"),
                ),
            )!!.constructors.single(),
        ).apply {
            putValueArgument(
                0,
                irCall(
                    pluginContext.referenceProperties(
                        CallableId(FqName("kotlin.jvm"), Name.identifier("javaObjectType")),
                    ).first().owner.getter!!,
                ).apply {
                    extensionReceiver = kClassReference(type)
                },
            )
            putValueArgument(1, typeArray)
        }
    }
}
