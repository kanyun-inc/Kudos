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

plugins {
    kotlin("jvm")
    java
    id("com.bennyhuo.kotlin.trimindent")
    id("com.github.gmazzo.buildconfig")
    id("com.bennyhuo.kotlin.plugin.embeddable.test")
}

tasks.withType<Test> {
    systemProperty("KOTLIN_COMPILER", project.property("KOTLIN_COMPILER") ?: "K2")
    systemProperty("VARIANT", project.property("VARIANT") ?: "gson")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler")

    implementation("org.jetbrains.kotlin:kotlin-noarg:1.8.20")

    testImplementation(kotlin("test-junit"))
    testImplementation(project(":kudos-gson"))
    testImplementation(project(":kudos-jackson"))
    testImplementation(project(":kudos-android-json-reader"))
    testImplementation(project(":android-json-reader"))
    testImplementation("org.jetbrains.kotlin:kotlin-noarg:1.8.20")
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    testImplementation("com.bennyhuo.kotlin:kotlin-compile-testing-extensions:1.8.20-1.2.2")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.freeCompilerArgs += listOf(
    "-Xjvm-default=enable",
    "-Xcontext-receivers",
    "-opt-in=kotlin.RequiresOptIn"
)
compileKotlin.kotlinOptions.jvmTarget = "1.8"

buildConfig {
    packageName("$group")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${project.property("KOTLIN_PLUGIN_ID")}\"")
}
