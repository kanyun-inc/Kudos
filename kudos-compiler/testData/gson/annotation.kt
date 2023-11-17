// SOURCE
// FILE: Main.kt
package com.kanyun.kudos.test

import com.kanyun.kudos.annotations.Kudos
import com.kanyun.kudos.gson.KUDOS_GSON

@Kudos(KUDOS_GSON)
class Desc(val descDetail: String)

@Kudos(KUDOS_GSON)
class Project(val projectName: String, val projectId: Int, val tags: List<String>,val desc: Desc)

// EXPECT
// FILE: compiles.log
OK
// FILE: Main.kt.ir
package com.kanyun.kudos.test
@Kudos(value = 1)
@JsonAdapter(value = KudosReflectiveTypeAdapterFactory::class)
class Desc(val descDetail: String) : KudosValidator {
    constructor{
        ctor<Any>()
        init<Desc>()
    }
    override fun validate(status: Map<String, Boolean>) {
        validateField("descDetail", status)
    }
}
@Kudos(value = 1)
@JsonAdapter(value = KudosReflectiveTypeAdapterFactory::class)
class Project(val projectName: String, val projectId: Int, val tags: List<String>, val desc: Desc) : KudosValidator {
    constructor{
        ctor<Any>()
        init<Project>()
    }
    override fun validate(status: Map<String, Boolean>) {
        validateField("projectName", status)
        validateField("projectId", status)
        validateField("tags", status)
        validateField("desc", status)
        validateCollection("tags", <this>.tags, "List")
    }
}