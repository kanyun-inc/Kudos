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

import com.kanyun.kudos.compiler.k1.KudosComponentContainerContributor
import com.kanyun.kudos.compiler.k1.KudosSyntheticResolveExtension
import com.kanyun.kudos.compiler.k2.KudosFirExtensionRegistrar
import com.kanyun.kudos.compiler.options.Option
import com.kanyun.kudos.compiler.utils.Logger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.noarg.NoArgConfigurationKeys
import org.jetbrains.kotlin.noarg.NoArgPluginNames
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

lateinit var logger: Logger

@OptIn(ExperimentalCompilerApi::class)
class KudosCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        Option.initialize(configuration)

        logger = Logger(configuration.get(CLIConfigurationKeys.ORIGINAL_MESSAGE_COLLECTOR_KEY)!!)

        val noArgAnnotations = configuration.get(NoArgConfigurationKeys.ANNOTATION).orEmpty().toMutableList()
        configuration.get(NoArgConfigurationKeys.PRESET)?.forEach { preset ->
            NoArgPluginNames.SUPPORTED_PRESETS[preset]?.let { noArgAnnotations += it }
        }
        val kudosAnnotationValueMap = hashMapOf<String, List<Int>>()
        IrGenerationExtension.registerExtension(KudosIrGenerationExtension(kudosAnnotationValueMap))
        SyntheticResolveExtension.registerExtension(KudosSyntheticResolveExtension(kudosAnnotationValueMap))
        StorageComponentContainerContributor.registerExtension(KudosComponentContainerContributor(noArgAnnotations))
        FirExtensionRegistrarAdapter.registerExtension(KudosFirExtensionRegistrar(noArgAnnotations, kudosAnnotationValueMap))
    }

    override val supportsK2: Boolean
        get() = true
}
