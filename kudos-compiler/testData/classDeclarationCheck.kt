// SOURCE
// FILE: Main.kt
import com.kanyun.kudos.annotations.Kudos
import com.google.gson.Gson
import com.google.gson.annotations.JsonAdapter
import com.kanyun.kudos.gson.adapter.KudosReflectiveTypeAdapterFactory

@Kudos
class User(val id: Long, val name: String)

@Kudos
class User2(val id: Long, val name: String, city: String="beijing") {
    val city: String = city
}

const val CONST_VALUE = "hello"

@Kudos
class User3(val id: Long, val name: String) {
    init {
        println("This is illegal.")
    }

    val firstName = name.split(" ").first()

    val lastName by lazy {
        name.split(" ").last()
    }

    val notStringliteral = "s$id"
    val thisExpr = "s${this.id}"
    val funReference = initValue()

    fun initValue() = "Hello"

    val stringliteral = "s"
    val stringliteral2 = "s $CONST_VALUE"
    val stringliteral3 = "Hello" + "World" + CONST_VALUE + 1
    val intLiteral = 1
    val uintLiteral = 1u
    val floatLiteral = 1f
    val booleanLiteral = false
    val nullValue: String? = null
}

@Kudos
data class Generic<T>(val t: T)


// EXPECT
// FILE: compiles.log
COMPILATION_ERROR
e: Main.kt: (9, 1): Primary constructor must only have property (val / var) parameters in class annotated with @Kudos.
e: Main.kt: (16, 1): Init blocks are forbidden in class annotated with @Kudos.
e: Main.kt: (22, 5): Property 'firstName' with initializer accessing other members is forbidden in class annotated with @Kudos.
e: Main.kt: (28, 5): Property 'notStringliteral' with initializer accessing other members is forbidden in class annotated with @Kudos.
e: Main.kt: (29, 5): Property 'thisExpr' with initializer accessing other members is forbidden in class annotated with @Kudos.
e: Main.kt: (30, 5): Property 'funReference' with initializer accessing other members is forbidden in class annotated with @Kudos.
e: Main.kt: (44, 1): Generic type is not supported. You can declare a subclass of it providing concrete type arguments.
