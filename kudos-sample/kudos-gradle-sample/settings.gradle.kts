pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }

    val KUDOS_VERSION: String by settings
    plugins {
        id("com.kanyun.kudos") version(KUDOS_VERSION)
    }
}
