// SOURCE
// MODULE: a
// FILE: Person.kt
package com.kanyun.kudos.test

import com.kanyun.kudos.annotations.Kudos
import com.kanyun.kudos.validator.KudosValidator

open class Common : KudosValidator {
    override fun validate(status: Map<String, Boolean>) {
        super.validate(status)
        println("in common")
    }
}

@Kudos
open class Person : Common() {
    val id: Long = 0
    val name: String = "Bob"

    override fun validate(status: Map<String, Boolean>) {
        super.validate(status)
        println("in Person")
    }
}

// FILE: User.kt
package com.kanyun.kudos.test

import com.kanyun.kudos.annotations.Kudos

@Kudos
class User {
    val id: Long = 0
    val name: String = "Bob"

    override fun validate(status: Map<String, Boolean>) {
        super.validate(status)
        println("in User")
    }
}

// MODULE: b / a
// FILE: Developer.kt
package com.kanyun.kudos.test

import com.kanyun.kudos.annotations.Kudos
import com.kanyun.kudos.validator.KudosValidator
import com.kanyun.kudos.test.Person

@Kudos
class Developer(val company: String) : Person() {
    override fun toString(): String {
        return super.toString()
    }
}

// FILE: Main.kt[MainKt#main]
import com.kanyun.kudos.validator.KudosValidator
import com.kanyun.kudos.test.Developer
import com.kanyun.kudos.test.User

fun main() {
    (User() as KudosValidator).validate(emptyMap())
    (Developer("kanyun") as KudosValidator).validate(emptyMap())
}

// EXPECT
// MODULE: b
// FILE: MainKt.main.stdout
in User
in common
in Person
