// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos
@Kudos
class Desc(val descDetail: String)

@Kudos
class UserLazy(val id: Long, val name: String, val desc: Desc, val tags: List<List<String>>) {
    val firstName by lazy {
        name.split(" ").first()
    }

    val lastName by lazy {
        name.split(" ").last()
    }

    override fun toString(): String {
        return "UserLazy(id=$id, name=$name, firstName=$firstName, lastName=$lastName, desc=${desc.descDetail}, tags=$tags"
    }
}

fun main() {
    deserialize<UserLazy>("""{"id": 10, "name": "John Claud", "desc": {"descDetail": "desc detail"}, "tags": [["tag1", "tag2"],["abc","def"]] }""")
}

// EXPECT
// FILE: MainKt.main.stdout
UserLazy(id=10, name=John Claud, firstName=John, lastName=Claud, desc=desc detail, tags=[[tag1, tag2], [abc, def]]
