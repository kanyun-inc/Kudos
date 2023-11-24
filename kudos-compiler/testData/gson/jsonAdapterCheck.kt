// SOURCE
// FILE: Main.kt
import com.kanyun.kudos.annotations.Kudos
import com.google.gson.Gson
import com.google.gson.annotations.JsonAdapter
import com.kanyun.kudos.gson.adapter.KudosReflectiveTypeAdapterFactory

@JsonAdapter(KudosReflectiveTypeAdapterFactory::class)
@Kudos
class User(val id: Long, val name: String)

// EXPECT
// FILE: compiles.log
COMPILATION_ERROR
e: Main.kt: (6, 1): Class annotated with @Kudos should not annotated with @JsonAdapter explicitly.
