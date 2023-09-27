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

package com.kanyun.kudos.compiler.utils

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.util.allOverridden
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.FqName

/**
 * Created by Benny Huo on 2023/9/8
 */
fun IrFunction.irThis(): IrExpression {
    val irDispatchReceiverParameter = dispatchReceiverParameter!!
    return IrGetValueImpl(
        startOffset,
        endOffset,
        irDispatchReceiverParameter.type,
        irDispatchReceiverParameter.symbol,
    )
}

fun IrSimpleFunction.overridesFunctionIn(fqName: FqName): Boolean =
    parentClassOrNull?.fqNameWhenAvailable == fqName || allOverridden().any {
        it.parentClassOrNull?.fqNameWhenAvailable == fqName
    }
