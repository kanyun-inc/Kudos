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

package com.kanyun.kudos.gradle

import com.kanyun.kudos.kudos.BuildConfig
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

open class KudosGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        val kudosExtension = target.extensions.create("kudos", KudosExtension::class.java)

        operator fun Configuration.plusAssign(dependency: String) {
            dependencies.add(target.dependencies.create(dependency))
        }

        target.afterEvaluate {
            target.configurations.all { config ->
                val name = config.name
                if (name != "api") return@all

                if (kudosExtension.gson) {
                    config += "${BuildConfig.KOTLIN_PLUGIN_GROUP}:kudos-gson:${BuildConfig.KOTLIN_PLUGIN_VERSION}"
                }

                if (kudosExtension.jackson) {
                    config += "${BuildConfig.KOTLIN_PLUGIN_GROUP}:kudos-jackson:${BuildConfig.KOTLIN_PLUGIN_VERSION}"
                }

                config += "${BuildConfig.KOTLIN_PLUGIN_GROUP}:kudos-annotations:${BuildConfig.KOTLIN_PLUGIN_VERSION}"
                config += "${BuildConfig.KOTLIN_PLUGIN_GROUP}:kudos-runtime:${BuildConfig.KOTLIN_PLUGIN_VERSION}"
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
        artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
        version = BuildConfig.KOTLIN_PLUGIN_VERSION,
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val kudosExtension = project.extensions.getByType(KudosExtension::class.java)
        val options = ArrayList<SubpluginOption>()
        if (kudosExtension.gson) {
            options += SubpluginOption("isGsonEnabled", "true")
        }
        if (kudosExtension.jackson) {
            options += SubpluginOption("isJacksonEnabled", "true")
        }
        return project.provider { options }
    }
}
