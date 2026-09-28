import de.florianreuth.baseproject.viaRelease

plugins {
    `java-library`
    alias(libs.plugins.hangar.publish)
    id("base.java")
    id("via.maven_publish")
}

dependencies {
    compileOnly(libs.viaversion.api)
    compileOnly(libs.spigot.api)
}

tasks {
    processResources {
        val projectVersion = project.version
        val projectDescription = project.description
        filesMatching("plugin.yml") {
            expand(mapOf("version" to projectVersion, "description" to projectDescription))
        }
    }
}

val release = viaRelease("master")
hangarPublish {
    publications.register("plugin") {
        version.set(release.version)
        id.set("ViaRewindLegacySupport")
        channel.set(release.hangarChannel)
        changelog.set(release.commitChangelog)
        apiKey.set(System.getenv("HANGAR_TOKEN"))
        platforms {
            paper {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set(listOf(property("minecraft_version_range") as String))
                dependencies.hangar("ViaVersion") {
                    required.set(true)
                }
                dependencies.hangar("ViaBackwards") {
                    required.set(false)
                }
                dependencies.hangar("ViaRewind") {
                    required.set(false)
                }
            }
        }
    }
}
tasks.named("publishPluginPublicationToHangar") {
    notCompatibleWithConfigurationCache("")
}
