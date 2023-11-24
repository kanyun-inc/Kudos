// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos

@Kudos
data class User(val id: Long?, val name: String = "Luca")

fun main() {
    deserialize<User>("""{}""")
    deserialize<User>("""{"id": 10}""")
    deserialize<User>("""{"name": "Bob"}""")
    deserialize<User>("""{"id": 10, "name": "Bob"}""")
}

// EXPECT
// FILE: MainKt.main.stdout
User(id=null, name=Luca)
User(id=10, name=Luca)
User(id=null, name=Bob)
User(id=10, name=Bob)
