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

package com.kanyun.kudos.compiler.k2.utils

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.FirThisReference
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid

/**
 * Created by Benny Huo on 2023/9/18
 */
fun FirPropertySymbol.initializedWithThisReference(): Boolean {
    val initializer = resolvedInitializer ?: return false
    var result = false
    initializer.accept(object : FirVisitorVoid() {
        override fun visitElement(element: FirElement) {
            if (!result) element.acceptChildren(this)
        }

        override fun visitThisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression) {
            result = true
        }

        override fun visitThisReference(thisReference: FirThisReference) {
            result = true
        }
    })
    return result
}
