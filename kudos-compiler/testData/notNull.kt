// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos
import com.kanyun.kudos.collections.KudosCollection
import com.kanyun.kudos.collections.KudosList
import com.kanyun.kudos.collections.KudosSet

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

fun main() {
    deserialize<User>("""{}""")
    deserialize<User>("""{"id": 10}""")
    deserialize<User>("""{"name": "Bod"}""")
    deserialize<List<User>>("""[{"id": 10}, {"id": 11}]""")
    deserialize<KudosCollection<User>>("""[{"id": 10, "name": "Bob"}]""")
    deserialize<KudosCollection<User>>("""[null]""")
    deserialize<KudosList<User>>("""[null]""")
    deserialize<KudosSet<User>>("""[null]""")

    // Maybe supported with Java 8 annotated type. But ... not the moment.
    deserialize<List<User>>("""[null]""")
    deserialize<User>("""{"name": "Bob"}""")
    deserialize<User>("""{"id": 10, "name": "Bob"}""")

    deserialize<Collections>("""{"list": [null], "list2": []}""")
    deserialize<Collections>("""{"list": ["kudos"], "list2": [null]}""")

    deserialize<Arrays>("""{"array": [null], "array2": []}""")
    deserialize<Arrays>("""{"array": ["kudos"], "array2": [null]}""")
}

// EXPECT
// FILE: MainKt.main.stdout
java.lang.NullPointerException: Missing non-null field 'id'.
java.lang.NullPointerException: Missing non-null field 'name'.
java.lang.NullPointerException: Missing non-null field 'id'.
java.lang.NullPointerException: Missing non-null field 'name'.
[User(id=10, name=Bob, age=10)]
java.lang.NullPointerException: Element cannot be null for com.kanyun.kudos.collections.KudosCollection.
java.lang.NullPointerException: Element cannot be null for com.kanyun.kudos.collections.KudosList.
java.lang.NullPointerException: Element cannot be null for com.kanyun.kudos.collections.KudosSet.
[null]
java.lang.NullPointerException: Missing non-null field 'id'.
User(id=10, name=Bob, age=10)
java.lang.NullPointerException: Element must not be null in List 'list'.
Collections(list=[kudos], list2=[null], list3=null)
java.lang.NullPointerException: Element must not be null in array 'array'.
Arrays(array=[kudos], array2=[null], array3=null)
