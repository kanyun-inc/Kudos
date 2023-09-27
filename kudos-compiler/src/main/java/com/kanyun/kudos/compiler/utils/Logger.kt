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

package com.kanyun.kudos.compiler.utils

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

class Logger(private val messageCollector: MessageCollector) {

    fun info(message: Any?) {
        println(CompilerMessageSeverity.INFO, message)
    }

    fun warn(message: Any?) {
        println(CompilerMessageSeverity.WARNING, message)
    }

    fun error(message: Any?) {
        println(CompilerMessageSeverity.ERROR, message)
    }

    fun println(level: CompilerMessageSeverity, message: Any?) {
        messageCollector.report(level, "[Kudos] $message")
    }
}
