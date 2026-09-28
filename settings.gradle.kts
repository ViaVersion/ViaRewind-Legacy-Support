pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("base.settings")
}

dependencyResolutionManagement {
    repositories {
        maven("https://repo.viaversion.com")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

rootProject.name = "viarewind-legacy-support"
