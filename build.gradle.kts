import de.florianmichael.baseproject.*

plugins {
    `java-library`
    id("io.papermc.hangar-publish-plugin")
    id("de.florianmichael.baseproject.BaseProject")
}

repositories {
    maven("https://repo.viaversion.com")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.maven.apache.org/maven2/")
}

setupProject()
setupViaPublishing()

dependencies {
    compileOnly("com.viaversion:viaversion-api:4.10.0")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
}

base {
    archivesName.set("ViaRewind-Legacy-Support")
}

tasks {
    processResources {
        val projectVersion = project.version
        val projectDescription = project.description
        filesMatching("plugin.yml") {
            expand("version" to projectVersion, "description" to projectDescription)
        }
    }
}

val branch = branchName()
val baseVersion = version as String
val isRelease = !baseVersion.contains('-')
val suffixedVersion = if (isRelease) baseVersion else baseVersion + "+" + System.getenv("GITHUB_RUN_NUMBER")
val commitHash = latestCommitHash()
val changelogContent = "[${commitHash}](https://github.com/ViaVersion/iaRewind-Legacy-Support/commit/${commitHash}) ${latestCommitMessage()}"
val isMainBranch = branch == "master"
hangarPublish {
    publications.register("plugin") {
        version.set(suffixedVersion)
        id.set("ViaRewindLegacySupport")
        channel.set(if (isRelease) "Release" else if (isMainBranch) "Snapshot" else "Alpha")
        changelog.set(changelogContent)
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
