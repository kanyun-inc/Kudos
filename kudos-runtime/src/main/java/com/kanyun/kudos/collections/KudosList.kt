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

package com.kanyun.kudos.collections

import com.kanyun.kudos.utils.elementRequiresNotNull

/**
 * Created by Benny Huo on 2023/9/6
 */
class KudosList<E : Any> : ArrayList<E>(), KudosCollection<E>, MutableList<E> {
    override fun add(element: E): Boolean {
        return super.add(element)
    }

    override fun add(index: Int, element: E) {
        super.add(index, element)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach { elementRequiresNotNull(it) }
        return super.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        elements.forEach { elementRequiresNotNull(it) }
        return super.addAll(index, elements)
    }

    override fun set(index: Int, element: E): E {
        return super.set(index, element)
    }
}
