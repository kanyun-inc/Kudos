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
class Project(val id: Int,val projectDesc: Desc)

@Kudos
class Desc(val des: String)

fun main() {
    deserialize<Project>("""{"id": 10, "projectDesc": null}""")
}

// EXPECT
// FILE: MainKt.main.stdout
java.lang.NullPointerException: Missing non-null field 'projectDesc'.