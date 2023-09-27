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

package com.kanyun.kudos.compiler.k2.diagnostic

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory

/**
 * Created by Benny Huo on 2023/8/21
 */
object KudosKtErrors {

    val CONFLICTS_WITH_NOARG by error1<PsiClass, String>()
    val CONFLICTS_WITH_JSON_ADAPTER by error0<PsiClass>()
    val NOT_PROPERTY_PARAMETER by error0<PsiElement>()
    val VARARG_PARAMETER by error0<PsiElement>()
    val INIT_BLOCK by error0<PsiElement>()
    val PROPERTY_INITIALIZER by error1<PsiElement, String>()
    val PROPERTY_DELEGATE by error1<PsiElement, String>()
    val NON_KUDOS_PROPERTY_TYPE by error1<PsiElement, String>()
    val GENERIC_TYPE by error0<PsiElement>()

    init {
        RootDiagnosticRendererFactory.registerFactory(KudosKtDefaultErrorMessages)
    }
}
