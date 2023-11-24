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

import com.kanyun.kudos.compiler.utils.hasKudosAnnotation
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

/**
 * Created by Benny Huo on 2023/8/21
 */
class KudosIrTransformer(
    private val context: IrPluginContext,
    private val kudosAnnotationValueMap: HashMap<String, List<Int>>,
) : IrElementVisitorVoid {
    private val noArgConstructors = mutableMapOf<IrClass, IrConstructor>()

    override fun visitElement(element: IrElement) {
        element.acceptChildren(this, null)
    }

    override fun visitClass(declaration: IrClass) {
        super.visitClass(declaration)
        if (declaration.kind != ClassKind.CLASS) return
        if (!declaration.hasKudosAnnotation()) return

        KudosIrClassTransformer(context, declaration, noArgConstructors, kudosAnnotationValueMap).transform()
    }
}
