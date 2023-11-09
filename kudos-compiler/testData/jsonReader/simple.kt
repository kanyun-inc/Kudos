// SOURCE
// FILE: Main.kt
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
                }
                else -> {
                    jsonReader.skipValue()
                }
            }
        }
        jsonReader.endObject()
        return <this>
    }
    constructor{
        ctor<Any>()
        init<Desc>()
    }
    override fun validate(status: Map<String, Boolean>) {
        validateField("descDetail", status)
    }
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
                }
                tmp0 == "projectId" -> {
                    <this>.projectId = jsonReader.nextInt()
                }
                tmp0 == "tags" -> {
                    <this>.tags = parseKudosObject(jsonReader, ParameterizedTypeImpl(List<String>::class.javaObjectType, arrayOf(String::class.javaObjectType)))
                }
                tmp0 == "desc" -> {
                    <this>.desc = parseKudosObject(jsonReader, Desc::class.javaObjectType)
                }
                else -> {
                    jsonReader.skipValue()
                }
            }
        }
        jsonReader.endObject()
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
}