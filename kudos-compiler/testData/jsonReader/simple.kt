// SOURCE
// FILE: Main.kt
package com.kanyun.kudos.test

import com.kanyun.kudos.annotations.Kudos

@Kudos
class Desc(val descDetail: String)

@Kudos
class Project(val projectName: String, val projectId: Int, val tags: List<String>,val desc: Desc)

// EXPECT
// FILE: compiles.log
OK
// FILE: Main.kt.ir
package com.kanyun.kudos.test
@Kudos
class Desc(val descDetail: String) : KudosValidator, KudosJsonAdapter<Desc> {
    override fun fromJson(jsonReader: JsonReader): Desc {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val tmp0 = jsonReader.nextName()
            when {
                tmp0 == "descDetail" -> {
                    <this>.descDetail = jsonReader.nextString()
                    <this>.kudosFieldStatusMap.put("descDetail", <this>.descDetail != null)
                }
                else -> {
                    jsonReader.skipValue()
                }
            }
        }
        jsonReader.endObject()
        validate(<this>.kudosFieldStatusMap)
        return <this>
    }
    constructor{
        ctor<Any>()
        init<Desc>()
    }
    override fun validate(status: Map<String, Boolean>) {
        validateField("descDetail", status)
    }
    private var kudosFieldStatusMap: Map<String, Boolean> = hashMapOf()
}
@Kudos
class Project(val projectName: String, val projectId: Int, val tags: List<String>, val desc: Desc) : KudosValidator, KudosJsonAdapter<Project> {
    override fun fromJson(jsonReader: JsonReader): Project {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val tmp0 = jsonReader.nextName()
            when {
                tmp0 == "projectName" -> {
                    <this>.projectName = jsonReader.nextString()
                    <this>.kudosFieldStatusMap.put("projectName", <this>.projectName != null)
                }
                tmp0 == "projectId" -> {
                    <this>.projectId = jsonReader.nextInt()
                    <this>.kudosFieldStatusMap.put("projectId", <this>.projectId != null)
                }
                tmp0 == "tags" -> {
                    <this>.tags = parseKudosObject(jsonReader, ParameterizedTypeImpl(List<String>::class.javaObjectType, arrayOf(String::class.javaObjectType)))
                    <this>.kudosFieldStatusMap.put("tags", <this>.tags != null)
                }
                tmp0 == "desc" -> {
                    <this>.desc = parseKudosObject(jsonReader, Desc::class.javaObjectType)
                    <this>.kudosFieldStatusMap.put("desc", <this>.desc != null)
                }
                else -> {
                    jsonReader.skipValue()
                }
            }
        }
        jsonReader.endObject()
        validate(<this>.kudosFieldStatusMap)
        return <this>
    }
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
    private var kudosFieldStatusMap: Map<String, Boolean> = hashMapOf()
}