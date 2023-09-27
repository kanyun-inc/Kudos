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
plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    implementation(kotlin("stdlib"))
}

buildConfig {
    val compilerPluginProject = project(":kudos-compiler-embeddable")
    packageName("${compilerPluginProject.group}.kudos")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${property("KOTLIN_PLUGIN_ID")}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${compilerPluginProject.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${compilerPluginProject.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${compilerPluginProject.version}\"")
}

gradlePlugin {
    plugins {
        create("KudosGradlePlugin") {
            id = "com.kanyun.kudos"
            displayName = "Kudos plugin"
            description = "Kudos plugin"
            implementationClass = "com.kanyun.kudos.gradle.KudosGradlePlugin"
        }
    }
}
