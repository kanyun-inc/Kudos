pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

include(":kudos-compiler")
include(":kudos-compiler-embeddable")
include(":kudos-gradle-plugin")
include(":kudos-maven-plugin")
include(":kudos-annotations")
include(":kudos-runtime")
include(":kudos-gson")
include(":kudos-jackson")
