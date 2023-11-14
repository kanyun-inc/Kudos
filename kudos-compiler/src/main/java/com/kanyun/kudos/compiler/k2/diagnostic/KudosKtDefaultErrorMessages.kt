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

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

object KudosKtDefaultErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP = KtDiagnosticFactoryToRendererMap("Kudos").also { map ->
        map.put(
            KudosKtErrors.CONFLICTS_WITH_NOARG,
            "Class annotated with @Kudos conflicts with no-arg annotation @{0}.",
            Renderers.TO_STRING,
        )

        map.put(
            KudosKtErrors.CONFLICTS_WITH_JSON_ADAPTER,
            "Class annotated with @Kudos should not annotated with @JsonAdapter explicitly.",
        )

        map.put(
            KudosKtErrors.NOT_PROPERTY_PARAMETER,
            "Primary constructor must only have property (val / var) parameters in class annotated with @Kudos.",
        )

        map.put(
            KudosKtErrors.VARARG_PARAMETER,
            "Vararg parameters are forbidden in primary constructor for class annotated with @Kudos.",
        )

        map.put(
            KudosKtErrors.INIT_BLOCK,
            "Init blocks are forbidden in class annotated with @Kudos.",
        )

        map.put(
            KudosKtErrors.PROPERTY_INITIALIZER,
            "Property ''{0}'' with initializer accessing other members is forbidden in class annotated with @Kudos.",
            Renderers.TO_STRING,
        )

        map.put(
            KudosKtErrors.PROPERTY_DELEGATE,
            "Property ''{0}'' with delegate is forbidden in class annotated with @Kudos.",
            Renderers.TO_STRING,
        )

        map.put(
            KudosKtErrors.NON_KUDOS_PROPERTY_TYPE,
            "''{0}'' is not supported by Kudos. Annotate the class with @Kudos or" +
                " suppress the error by annotating the property with @KudosIgnore.",
            Renderers.TO_STRING,
        )

        map.put(
            KudosKtErrors.GENERIC_TYPE,
            "Generic type is not supported. You can declare a subclass of it providing concrete type arguments.",
        )
    }
}
