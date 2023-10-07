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

package com.kanyun.kudos.compiler.base

import com.bennyhuo.kotlin.compiletesting.extensions.module.COMPILER_OUTPUT_LEVEL_WARN
import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_TYPE_RAW
import com.bennyhuo.kotlin.compiletesting.extensions.module.KotlinModule
import com.bennyhuo.kotlin.compiletesting.extensions.module.checkResult
import com.bennyhuo.kotlin.compiletesting.extensions.source.TextBasedModuleInfoLoader
import com.kanyun.kudos.compiler.KudosCompilerPluginRegistrar
import com.kanyun.kudos.compiler.options.Options
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.File

/**
 * Created by benny at 2022/1/15 8:55 PM.
 */
@OptIn(ExperimentalCompilerApi::class)
open class TestBase {

    private val validVariants = arrayOf("gson", "jackson")

    private val useK2 = System.getProperty("KOTLIN_COMPILER") == "K2"
    private val variant = System.getProperty("VARIANT") ?: validVariants.first()

    init {
        println("[Kudos] Testing with K2 enabled=$useK2 and variant=$variant.")
    }

    private fun firstExistFile(vararg paths: String): String {
        return paths.flatMap { path ->
            // ignore cases of the first letter
            listOf(path.replaceFirstChar { it.uppercaseChar() }, path.replaceFirstChar { it.lowercaseChar() })
        }.firstOrNull {
            File(it).exists()
        } ?: throw IllegalArgumentException("Files not found: ${paths.contentToString()}")
    }

    @Suppress("NOTHING_TO_INLINE", "SameParameterValue")
    private inline fun functionName(index: Int): String {
        return Throwable().stackTrace[index].methodName
    }

    private fun gsonDeserialize(): String {
        Options.gson.set(true)

        return """
            // FILE: Gson.kt
            import com.google.gson.Gson
            import com.google.gson.GsonBuilder
            import com.google.gson.reflect.TypeToken
            import com.google.gson.annotations.JsonAdapter
            import com.kanyun.kudos.gson.kudosGson
            import com.kanyun.kudos.gson.adapter.KudosReflectiveTypeAdapterFactory
            
            inline fun <reified T: Any> deserialize(string: String): T? {
                val gson = kudosGson()
                return try {
                    val t: T = gson.fromJson(string, object: TypeToken<T>() {}.type)
                    println(t)
                    t
                } catch (e: Exception) {
                    println(e)
                    null
                }
            }
        """.trimIndent()
    }

    private fun jacksonDeserialize(): String {
        return """
            // FILE: Jackson.kt
            import com.kanyun.kudos.jackson.kudosObjectMapper
            import com.fasterxml.jackson.core.type.TypeReference
            import com.fasterxml.jackson.databind.DeserializationFeature
            
            inline fun <reified T: Any> deserialize(string: String): T? {
                val mapper = kudosObjectMapper()
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                mapper.disable(DeserializationFeature.WRAP_EXCEPTIONS);
                return try {
                    val t: T = mapper.readValue(string, object: TypeReference<T>() {})
                    println(t)
                    t
                } catch (e: Exception) {
                    println(e)
                    null
                }
            }
        """.trimIndent()
    }

    private fun doTest(fileName: String, deserializer: () -> String) {
        val filePath = firstExistFile("testData/$fileName", "testData/$fileName.kt", "testData/$fileName.txt")
        val deserializerSource = deserializer()
        val source = File(filePath).readText().replace("{{deserialize}}", deserializerSource)
        val loader = TextBasedModuleInfoLoader(source)
        val sourceModuleInfos = loader.loadSourceModuleInfos()
        val expectModuleInfo = loader.loadExpectModuleInfos()

        val modules = sourceModuleInfos.map {
            KotlinModule(
                it,
                compilerPluginRegistrars = listOf(KudosCompilerPluginRegistrar()),
                useK2 = useK2,
            )
        }

        modules.checkResult(
            expectModuleInfo,
            checkGeneratedIr = true,
            executeEntries = true,
            checkCompilerOutput = true,
            compilerOutputLevel = COMPILER_OUTPUT_LEVEL_WARN,
            irOutputType = IR_OUTPUT_TYPE_RAW,
        )
    }

    protected fun testBase(fileName: String = functionName(1)) {
        val testVariant = fileName.split("_").first().takeIf { it in validVariants }
        if (testVariant != null && testVariant != variant) {
            println("[Kudos] Skip test '$fileName' for '$testVariant' only. Current variant=$variant.")
            return
        }

        when (variant.toLowerCaseAsciiOnly()) {
            "gson" -> {
                doTest(fileName, ::gsonDeserialize)
            }
            "jackson" -> {
                doTest(fileName, ::jacksonDeserialize)
            }
            else -> {
                throw UnsupportedOperationException("Unknown variant '$variant'. Supported values: 'gson', 'jackson'.")
            }
        }
    }
}
