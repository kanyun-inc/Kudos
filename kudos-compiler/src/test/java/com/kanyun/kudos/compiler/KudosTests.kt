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

    @Test
    fun `common_classDeclarationCheck`() = testBase()

    @Test
    fun `common_constructor`() = testBase()

    @Test
    fun `common_defaultValue`() = testBase()

    @Test
    fun `common_initBlock`() = testBase()

    @Test
    fun `common_notNull`() = testBase()

    @Test
    fun `common_propertyTypeCheck`() = testBase()

    @Test
    fun `common_validator`() = testBase()

    @Test
    fun `gson_jsonAdapterCheck`() = testBase()

    @Test
    fun `gson_notNull`() = testBase()

    @Test
    fun `jsonReader_deserialize`() = testBase()

    @Test
    fun `jsonReader_deserializeArrayType`() = testBase()

    @Test
    fun `jsonReader_deserializeFloatType`() = testBase()

    @Test
    fun `jsonReader_deserializeMapType`() = testBase()

    @Test
    fun `jsonReader_deserializeSetType`() = testBase()

    @Test
    fun `jsonReader_notNull`() = testBase()

    @Test
    fun `jsonReader_simple`() = testBase()
}
