// SOURCE
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos
import com.google.gson.Gson
import com.google.gson.annotations.JsonAdapter
import com.kanyun.kudos.gson.adapter.KudosReflectiveTypeAdapterFactory

@Kudos
data class User(val id: Long, val name: String)

@Kudos
data class User2(val id: Long?, val name: String = "Luca")

fun main() {
    println(User(10, "Luca"))
    println(User2(100))
    println(User2(100, "Bob"))
}

// EXPECT
// FILE: MainKt.main.stdout
User(id=10, name=Luca)
User2(id=100, name=Luca)
User2(id=100, name=Bob)
