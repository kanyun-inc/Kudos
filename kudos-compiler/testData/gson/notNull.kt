import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl

// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos
import com.kanyun.kudos.collections.KudosCollection
import com.kanyun.kudos.collections.KudosList
import com.kanyun.kudos.collections.KudosSet
import com.kanyun.kudos.json.reader.adapter.ParameterizedTypeImpl

@Kudos
data class User(val id: Long, val name: String, val age: Int = 10) {
    val city: String = "beijing"
}

@Kudos
data class Collections(
    val list: List<String>,
    val list2: List<String?>,
    val list3: List<String>?,
)
@Kudos
class Arrays(
    val array: Array<String>,
    val array2: Array<String?>,
    val array3: Array<String>?,
) {
    override fun toString(): String {
        return "Arrays(array=${array.contentToString()}, array2=${array2.contentToString()}, array3=${array3?.contentToString()})"
    }
}

@Kudos
class Project(val id: Int,val projectDesc: Desc)

@Kudos
class Desc(val des: String)

fun main() {
    deserialize<Project>("""{"id": 10, "projectDesc": null}""")
}

// EXPECT
// FILE: MainKt.main.stdout
java.lang.NullPointerException: Missing non-null field 'projectDesc'.