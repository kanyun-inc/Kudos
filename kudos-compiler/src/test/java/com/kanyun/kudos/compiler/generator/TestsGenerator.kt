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

package com.kanyun.kudos.compiler.generator

import java.io.File

/**
 * Created by Benny Huo on 2023/6/25
 */
fun main() {
    val outputFileRelativePath = "src/test/java/com/kanyun/kudos/compiler/KudosTests.kt"
    val caseDirectoryRelativePath = "testData"

    val workingDir = File(".")
    val projectDir = if (workingDir.endsWith("kudos-compiler")) {
        workingDir
    } else {
        File(workingDir, "kudos-compiler")
    }

    val outputFile = File(projectDir, outputFileRelativePath)
    val caseDir = File(projectDir, caseDirectoryRelativePath)

    val content = """
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

        import com.kanyun.kudos.compiler.base.TestBase
        import org.junit.Test

        /**
         * Created by Benny Huo
         */
        class KudosTests : TestBase() {
           
            ${caseDir.listFiles().orEmpty()
        .flatMap {
            if (it.isDirectory) {
                it.listFiles().orEmpty().toList()
            } else {
                listOf(it)
            }
        }
        .map {
            val parentDir = it.parentFile.name
            if (parentDir == "testData") {
                it.nameWithoutExtension
            }else {
                "${parentDir}_${it.nameWithoutExtension}"
            }
        }
        .sortedBy { it }
        .joinToString("\n") {
            "@Test\nfun `${it.replaceFirstChar { it.lowercaseChar() }}`() = testBase()\n"
        }
    }
        }
    """.trimIndent()

    outputFile.writeText(content)
}
