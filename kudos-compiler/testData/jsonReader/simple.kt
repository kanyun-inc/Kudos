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
class Desc : KudosValidator, KudosJsonAdapter<Desc> {
    constructor(descDetail: String) /* primary */ {
        super/*Any*/()
        /* <init>() */

    }

    val descDetail: String
        field = descDetail
        get

    /* fake */ override operator fun equals(other: Any?): Boolean
    /* fake */ override fun hashCode(): Int
    /* fake */ override fun toString(): String
    override fun fromJson(jsonReader: JsonReader): Desc {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) { // BLOCK
            val tmp0: @FlexibleNullability String? = jsonReader.nextName()
            when {
                EQEQ(arg0 = tmp0, arg1 = "descDetail") -> <this>.#descDetail = jsonReader.nextString()
                else -> jsonReader.skipValue()
            }
        }
        jsonReader.endObject()
        return <this>
    }

    constructor() {
        super/*Any*/()
        /* <init>() */

    }

    override fun validate(status: Map<String, Boolean>) {
        validateField(name = "descDetail", fieldStatus = status)
    }

}

@Kudos
class Project : KudosValidator, KudosJsonAdapter<Project> {
    constructor(projectName: String, projectId: Int, tags: List<String>, desc: Desc) /* primary */ {
        super/*Any*/()
        /* <init>() */

    }

    val projectName: String
        field = projectName
        get

    val projectId: Int
        field = projectId
        get

    val tags: List<String>
        field = tags
        get

    val desc: Desc
        field = desc
        get

    /* fake */ override operator fun equals(other: Any?): Boolean
    /* fake */ override fun hashCode(): Int
    /* fake */ override fun toString(): String
    override fun fromJson(jsonReader: JsonReader): Project {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) { // BLOCK
            val tmp0: @FlexibleNullability String? = jsonReader.nextName()
            when {
                EQEQ(arg0 = tmp0, arg1 = "projectName") -> <this>.#projectName = jsonReader.nextString()
                EQEQ(arg0 = tmp0, arg1 = "projectId") -> <this>.#projectId = jsonReader.nextInt()
                EQEQ(arg0 = tmp0, arg1 = "tags") -> <this>.#tags = parseKudosObject(jsonReader = jsonReader, type = ParameterizedTypeImpl(type = KClass::class.<get-java></* null */>(), typeArguments = arrayOf</* null */>(elements = [KClass::class.<get-java></* null */>()])))
                EQEQ(arg0 = tmp0, arg1 = "desc") -> <this>.#desc = parseKudosObject(jsonReader = jsonReader, type = KClass::class.<get-java></* null */>())
                else -> jsonReader.skipValue()
            }
        }
        jsonReader.endObject()
        return <this>
    }

    constructor() {
        super/*Any*/()
        /* <init>() */

    }

    override fun validate(status: Map<String, Boolean>) {
        validateField(name = "projectName", fieldStatus = status)
        validateField(name = "projectId", fieldStatus = status)
        validateField(name = "tags", fieldStatus = status)
        validateField(name = "desc", fieldStatus = status)
        validateCollection(name = "tags", collection = <this>.#tags, typeName = "List")
    }

}