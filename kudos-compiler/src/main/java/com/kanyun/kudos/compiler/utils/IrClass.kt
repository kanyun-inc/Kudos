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

import com.kanyun.kudos.compiler.KUDOS
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName

/**
 * Created by Benny Huo on 2023/9/8
 */
fun IrClass.hasKudosAnnotation(): Boolean {
    return hasAnnotation(FqName(KUDOS))
}

fun IrClass.isSubclassOfFqName(fqName: String): Boolean =
    fqNameWhenAvailable?.asString() == fqName || superTypes.any {
        it.erasedUpperBound.isSubclassOfFqName(
            fqName,
        )
    }

fun IrClass.addOverride(
    baseFqName: FqName,
    name: String,
    returnType: IrType,
    modality: Modality = Modality.FINAL,
): IrSimpleFunction = addFunction(name, returnType, modality).apply {
    overriddenSymbols = superTypes.mapNotNull { superType ->
        superType.classOrNull?.owner?.takeIf { superClass ->
            superClass.isSubclassOfFqName(
                baseFqName.asString(),
            )
        }
    }.flatMap { superClass ->
        superClass.functions.filter { function ->
            function.name.asString() == name && function.overridesFunctionIn(baseFqName)
        }.map { it.symbol }.toList()
    }
}
