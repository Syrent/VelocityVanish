import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.modrinth.minotaur.ModrinthExtension
import com.modrinth.minotaur.TaskModrinthUpload
import io.papermc.hangarpublishplugin.model.Platforms
import org.sayandev.Module
import org.sayandev.getRelocations
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.0.0"
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.modrinth.minotaur") version "2.8.7"
}

val slug = findProperty("slug")!! as String
description = findProperty("description")!! as String

fun executeGitCommand(vararg command: String): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", *command)
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}

fun latestCommitMessage(): String {
    return executeGitCommand("log", "--pretty=format:%s")
}

val versionString: String = findProperty("version")!! as String
val isRelease: Boolean = (System.getenv("HANGAR_BUILD_CHANNEL") ?: "Snapshot") == "Release"

val publishVersion = if (isRelease) versionString else "$versionString-build.${System.getenv("GITHUB_RUN_NUMBER")}"
val commitVersion = publishVersion + "-" + (System.getenv("GITHUB_SHA")?.substring(0, 7) ?: "local")
version = commitVersion

val changelogContent: String = latestCommitMessage()

tasks {
    publishAllPublicationsToHangar {
        this.dependsOn(shadowJar)
        this.mustRunAfter(shadowJar)
    }

    register("publishAllModulesModrinth") {
        dependsOn(named("modrinthPublicationBukkit"))
        dependsOn(named("modrinthPublicationVelocity"))
        dependsOn(named("modrinthPublicationBungee"))
    }

    val modrinthModules = listOf(Module.BUKKIT, Module.VELOCITY, Module.BUNGEECORD)
    for (module in modrinthModules) {
        register("modrinthPublication${module.title}", TaskModrinthUpload::class.java) {
            println("Publishing ${module.id} module to modrinth")
            modrinth {
                val modrinthApiKey = System.getenv("MODRINTH_API_TOKEN")
                val modrinthChangelog = if (System.getenv("MODRINTH_CHANGELOG").isNullOrEmpty()) changelogContent else System.getenv("MODRINTH_CHANGELOG")

                token.set(modrinthApiKey)
                projectId.set("${rootProject.property("modrinthProjectID")}")
                versionNumber.set((if (isRelease) versionString else publishVersion.replace("-build.", "-b").replace("-SNAPSHOT", "")) + "-${module.id.lowercase()}")
                versionType.set(System.getenv("MODRINTH_BUILD_CHANNEL") ?: "beta")
                when (module) {
                    Module.BUKKIT -> {
                        uploadFile.set(project(":sayanvanish-bukkit").tasks.shadowJar.flatMap { it.archiveFile })
                    }
                    Module.VELOCITY -> {
                        uploadFile.set(project(":sayanvanish-proxy:sayanvanish-proxy-velocity").tasks.shadowJar.flatMap { it.archiveFile })
                    }
                    Module.BUNGEECORD -> {
                        uploadFile.set(project(":sayanvanish-proxy:sayanvanish-proxy-bungeecord").tasks.shadowJar.flatMap { it.archiveFile })
                    }
                    else -> {
                        throw IllegalArgumentException("Unknown module $module")
                    }
                }
                gameVersions.set("${rootProject.property("modrinthMinecraftVersions")}".split(","))
                loaders.set(
                    when (module) {
                        Module.BUKKIT -> listOf("paper", "purpur", "spigot", "folia")
                        Module.VELOCITY -> listOf("velocity")
                        Module.BUNGEECORD -> listOf("bungeecord", "waterfall")
                        else -> throw IllegalArgumentException("Unknown module $module")
                    }
                )

                changelog.set(modrinthChangelog)

                syncBodyFrom.set(rootProject.file("README.md").readText())
            }
        }
    }
}

allprojects {
    group = findProperty("group")!! as String
    version = findProperty("version")!! as String

    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("kotlin")
    plugins.apply("com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        mavenLocal()
    }

    tasks {
        processResources {
            filesMatching(listOf("**plugin.yml", "**plugin.json")) {
                expand(
                    "version" to commitVersion,
                    "slug" to slug,
                    "name" to rootProject.name,
                    "description" to rootProject.description
                )
            }
        }
    }
}

subprojects {
    java {
        withSourcesJar()

        disableAutoTargetJvm()
    }

    dependencies {
        compileOnly(kotlin("stdlib", version = "2.0.0"))
    }

    tasks {
        jar {
            archiveClassifier.set("unshaded")
        }

        build {
            dependsOn(shadowJar)
        }

        withType<ShadowJar> {
            archiveFileName.set("${rootProject.name}-${commitVersion}-${this@subprojects.name.removePrefix("sayanvanish-")}.jar")
            archiveClassifier.set(null as String?)
            destinationDirectory.set(file(rootProject.projectDir.path + "/bin"))
            from("LICENSE")
//            minimize()
        }
    }

    artifacts.archives(tasks.shadowJar)

    tasks.named<Jar>("sourcesJar") {
        getRelocations().forEach { (from, to) ->
            val filePattern = Regex("(.*)${from.replace('.', '/')}((?:/|$).*)")
            val textPattern = Regex.fromLiteral(from)
            eachFile {
                filter {
                    it.replaceFirst(textPattern, to)
                }
                path = path.replaceFirst(filePattern, "$1${to.replace('.', '/')}$2")
            }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                shadow.component(this)
                artifact(tasks["sourcesJar"])
                this.version = versionString
                setPom(this)
            }
        }

        repositories {
            maven {
                name = "sayandevelopment-repo"
                url = uri("https://repo.sayandev.org/snapshots/")

                credentials {
                    username = System.getenv("REPO_SAYAN_USER") ?: project.findProperty("repo.sayan.user") as? String
                    password = System.getenv("REPO_SAYAN_TOKEN") ?: project.findProperty("repo.sayan.token") as? String
                }
            }
        }
    }
}

fun setPom(publication: MavenPublication) {
    publication.pom {
        name.set("sayanvanish")
        description.set(project.description)
        url.set("https://github.com/syrent/sayanvanish")
        licenses {
            license {
                name.set("GNU General Public License v3.0")
                url.set("https://github.com/syrent/sayanvanish/blob/master/LICENSE")
            }
        }
        developers {
            developer {
                id.set("syrent")
                name.set("abbas")
                email.set("syrent2356@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/syrent/sayanvanish.git")
            developerConnection.set("scm:git:ssh://github.com/syrent/sayanvanish.git")
            url.set("https://github.com/syrent/sayanvanish/tree/master")
        }
    }
}

hangarPublish {
    publications.register("plugin") {
        version.set(if (isRelease) versionString else publishVersion)
        channel.set(System.getenv("HANGAR_BUILD_CHANNEL") ?: "Snapshot")
        changelog.set(if (System.getenv("HANGAR_CHANGELOG").isNullOrEmpty()) changelogContent else System.getenv("HANGAR_CHANGELOG"))
        id.set(slug)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        platforms {
            register(Platforms.PAPER) {
                jar.set(project(":sayanvanish-bukkit").tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set((property("paperVersion") as String).split(",").map { it.trim() })
            }

            register(Platforms.VELOCITY) {
                jar.set(project(":sayanvanish-proxy:sayanvanish-proxy-velocity").tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set((property("velocityVersion") as String).split(",").map { it.trim() })
            }

            register(Platforms.WATERFALL) {
                jar.set(project(":sayanvanish-proxy:sayanvanish-proxy-bungeecord").tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set((property("waterfallVersion") as String).split(",").map { it.trim() })
            }
        }
    }
}