// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos

@Kudos
class User(val ids: Set<Int>) {

    override fun toString(): String {
        return "User(ids=${ids})"
    }
}

fun main() {
    deserialize<User>("""{"ids": [123, 456, 123]}""")
}

// EXPECT
// FILE: MainKt.main.stdout
User(ids=[123, 456])
