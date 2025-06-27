pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("de.florianmichael.baseproject.BaseProject") version "1.1.1"
        id("io.papermc.hangar-publish-plugin") version "0.1.3"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "viarewind-legacy-support"
