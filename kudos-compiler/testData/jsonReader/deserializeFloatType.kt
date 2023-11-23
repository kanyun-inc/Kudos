// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos

@Kudos
class User(val doubleId: Double, val floatId: Float) {

    override fun toString(): String {
        return "User(doubleId=$doubleId, floatId=$floatId)"
    }
}

fun main() {
    deserialize<User>("""{"doubleId": 10.1234, "floatId": "10.1234"}""")
}

// EXPECT
// FILE: MainKt.main.stdout
User(doubleId=10.1234, floatId=10.1234)
