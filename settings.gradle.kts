pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.florianreuth.de/releases")
    }

    plugins {
        id("de.florianreuth.baseproject") version "2.0.0"
        id("io.papermc.hangar-publish-plugin") version "0.1.3"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "viarewind-legacy-support"
