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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
}

// embed the source files of kudos compiler plugin.
sourceSets {
    main {
        java.srcDir(rootProject.file("kudos-compiler/src/main/java"))
        resources.srcDir(rootProject.file("kudos-compiler/src/main/resources"))
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-noarg:1.8.20")

    compileOnly("org.jetbrains.kotlin:kotlin-compiler")
    compileOnly("org.jetbrains.kotlin:kotlin-maven-plugin")
    compileOnly("org.apache.maven:maven-core:3.8.6")
}

buildConfig {
    val compilerPluginProject = project(":kudos-compiler-embeddable")
    packageName("${compilerPluginProject.group}")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${property("KOTLIN_PLUGIN_ID")}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${compilerPluginProject.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${compilerPluginProject.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${compilerPluginProject.version}\"")
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.freeCompilerArgs += listOf(
    "-Xcontext-receivers",
    "-opt-in=kotlin.RequiresOptIn"
)
compileKotlin.kotlinOptions.jvmTarget = "1.8"
