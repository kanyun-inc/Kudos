// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos

@Kudos
class User(val ids: Array<Int>) {

    override fun toString(): String {
        return "User(ids[1]=${ids[1]})"
    }
}

fun main() {
    deserialize<User>("""{"ids": [123, 456]}""")
}

// EXPECT
// FILE: MainKt.main.stdout
User(ids[1]=456)
