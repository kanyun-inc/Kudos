// SOURCE
{{deserialize}}
// FILE: Main.kt [MainKt#main]
import com.kanyun.kudos.annotations.Kudos

@Kudos
class User(val id: Int, val tag: String){
    override fun toString(): String {
        return "User(id=${id}, tag=${tag})"
    }
}

@Kudos
class UserMap(val itemMap: Map<String, User>) {

    override fun toString(): String {
        return "UserMap(user2=${itemMap["user2"]})"
    }
}

fun main() {
    deserialize<UserMap>("""{
  "itemMap": {
    "user1": {
      "id": 123,
      "tag": "tag1"
    },
    "user2": {
      "id": 456,
      "tag": "tag2"
    }
  }
}""")
}

// EXPECT
// FILE: MainKt.main.stdout
UserMap(user2=User(id=456, tag=tag2))
