// SOURCE
// MODULE: a / b
// FILE: Main.kt
package com.kanyun.kudos.test

import com.kanyun.kudos.annotations.Kudos
import com.kanyun.kudos.annotations.KudosIgnore

@Kudos
class User(
    val id: Long,
    val name: String?,
    val location: Location,
    @KudosIgnore
    val locationIgnored: Location,
    @Transient
    val locationTransient: Location,
    val locations: Array<Location>,
    @KudosIgnore
    val locationsIgnored: Map<String, Location>,
    val company: Company,
    val projects: List<Project>
) {
    val locationsNoBackingField: List<Location>
        get() = locations.toList()
}
class Location(val lat: Double, val lng: Double)

@Kudos
class Company(val name: String)

// MODULE: b
package com.kanyun.kudos.test

import com.kanyun.kudos.annotations.Kudos

@Kudos
class Project(val name: String)

// EXPECT
// MODULE: a
// FILE: compiles.log
COMPILATION_ERROR
e: Main.kt: (10, 19): 'com.kanyun.kudos.test.Location' is not supported by Kudos. Annotate the class with @Kudos or suppress the error by annotating the property with @KudosIgnore.
e: Main.kt: (15, 20): 'com.kanyun.kudos.test.Location' is not supported by Kudos. Annotate the class with @Kudos or suppress the error by annotating the property with @KudosIgnore.
