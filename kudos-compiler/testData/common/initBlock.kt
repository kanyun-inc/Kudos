// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos

@Kudos
class UserLazy(val id: Long, val name: String) {
    val firstName by lazy {
        name.split(" ").first()
    }

    val lastName by lazy {
        name.split(" ").last()
    }

    override fun toString(): String {
        return "UserLazy(id=$id, name=$name, firstName=$firstName, lastName=$lastName)"
    }
}

fun main() {
    deserialize<UserLazy>("""{"id": 10, "name": "John Claud"}""")
}

// EXPECT
// FILE: MainKt.main.stdout
UserLazy(id=10, name=John Claud, firstName=John, lastName=Claud)
