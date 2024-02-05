import io.papermc.hangarpublishplugin.model.Platforms
import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("org.screamingsandals.nms-mapper") version "1.4.6"
    id("xyz.jpenilla.run-paper") version "2.2.2"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.modrinth.minotaur") version "2.8.7"
}

fun executeGitCommand(vararg command: String): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", *command)
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}

fun latestCommitMessage(): String {
    return executeGitCommand("log", "-1", "--pretty=%B")
}

val versionString: String = findProperty("version")!! as String
val isRelease: Boolean = (System.getenv("HANGAR_BUILD_CHANNEL") ?: "Snapshot") == "Release"

val suffixedVersion: String = if (isRelease) {
    versionString
} else {
    versionString + "-build." + (System.getenv("GITHUB_RUN_NUMBER") ?: "development")
}

val commitVersion = suffixedVersion + "-" + (System.getenv("GITHUB_SHA")?.substring(0, 7) ?: "local")

val changelogContent: String = latestCommitMessage()

val slug = "velocityvanish"
group = "ir.syrent.velocityvanish"
version = commitVersion
description = "Modern vanish system with Velocity and Folia support"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.maven.apache.org/maven2/")

    // Velocity-API / PaperLib / Folia
    maven("https://repo.papermc.io/repository/maven-public/")

    // AdventureAPI/MiniMessage
    maven("https://oss.sonatype.org/content/repositories/snapshots/")

    // Spigot
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")

    // ProtocolLib
    maven("https://repo.dmulloy2.net/repository/public/")

    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    // EssentialsX
    maven("https://repo.essentialsx.net/releases/")

    // Update checker
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")

    // DiscordSRV
    maven("https://nexus.scarsz.me/content/groups/public/")

    // Mojang
    maven("https://libraries.minecraft.net/")

    // Cloud SNAPSHOT (Dev repository)
    maven("https://repo.masmc05.dev/repository/maven-snapshots/")

    // Velocitab
    maven("https://repo.william278.net/releases")

    //Dynmap
    maven("https://repo.mikeprimm.com/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("xyz.jpenilla:squaremap-api:1.2.3")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("org.spongepowered:configurate-yaml:4.2.0-yamlbranch-4")
    compileOnly("com.discordsrv:discordsrv:1.27.0")
    compileOnly("com.mojang:authlib:1.11")
    compileOnly("io.netty:netty-all:4.1.104.Final")
    compileOnly("net.william278:velocitab:1.5.2")
    compileOnly("us.dynmap:DynmapCoreAPI:3.7-SNAPSHOT")
    // SayanChat 2.8.1
    // ProCosmetics
    // ServerListPlus Velocity (From extradeps)
    // SunLight
    // NexEngine (From extradeps)
    // CMI (From extradeps)
    compileOnly(fileTree("libs"))

    implementation("io.papermc:paperlib:1.0.8")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    // gson does not exist in legacy minecraft version
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.github.cryptomorin:XSeries:9.8.1") { isTransitive = false }

    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.15.0")

    implementation("cloud.commandframework:cloud-paper:tooltips-SNAPSHOT")
    implementation("cloud.commandframework:cloud-minecraft-extras:tooltips-SNAPSHOT")

    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}



publishing {
    publications {
        register<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = rootProject.name

            artifact(tasks.shadowJar.get().archiveFile)
        }
    }

    publishing {
        repositories {
            maven {
                name = "syrent"
                url = uri("https://jitpack.io")
            }
        }
    }

    tasks.withType<PublishToMavenLocal> {
        dependsOn(tasks.shadowJar)
    }
}

val extraDependencies = mapOf(
    "CMI.jar" to "https://www.zrips.net/wp-content/uploads/2021/09/CMI9.0.0.0API.jar",
    "NexEngine.jar" to "https://github.com/nulli0n/NexEngine-spigot/releases/download/v2.2.11/NexEngine.jar",
    "SunLight.jar" to "https://www.dropbox.com/scl/fi/mmu7g32vj6oitkja65cws/SunLight-3.9.4.jar?rlkey=tqxgcy80yccxh80yzl24ubs7b&dl=1",
    "ServerListPlus.jar" to "https://ci.codemc.io/job/Minecrell/job/ServerListPlus/lastSuccessfulBuild/artifact/Velocity/build/libs/ServerListPlus-3.5.1-SNAPSHOT-Velocity.jar",
)

tasks {
    runServer {
        minecraftVersion("1.20.2")
    }

    runPaper {
        folia.registerTask()
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        dependsOn(clean)
    }

    processResources {
        filesMatching(listOf("**plugin.yml", "**plugin.json")) {
            expand(
                "version" to project.version as String,
                "slug" to slug,
                "name" to rootProject.name,
                "description" to project.description
            )
        }
    }

    val extraDeps = register("downloadExtraDependencies") {
        val libsDir = File("libs")
        libsDir.mkdirs()
        val ex = Executors.newCachedThreadPool()
        for (entry in extraDependencies) {
            val file = File(libsDir, entry.key)
            if (file.exists())
                continue
            ex.submit {
                println("Downloading ${entry.key} from ${entry.value}")
                URL(entry.value).openStream().use { s -> file.outputStream().use { it.write(s.readBytes()) } }
                println("Successfully downloaded ${entry.key} to ${file.path}")
            }
        }
        ex.shutdown()
        ex.awaitTermination(10, TimeUnit.SECONDS)
    }

    shadowJar {
        dependsOn(extraDeps)
        archiveFileName.set("${rootProject.name}_${project.version}.jar")
        exclude("META-INF/**")
        from("LICENSE")
        minimize()

        relocate("io.papermc.lib", "ir.syrent.velocityvanish.dependencies.io.papermc.lib")
        relocate("io.leangen", "ir.syrent.velocityvanish.dependencies.io.leangen")
        relocate("org.bstats", "ir.syrent.velocityvanish.dependencies.org.bstats")
        relocate("com.google.gson", "ir.syrent.velocityvanish.dependencies.com.google.gson")
        relocate("com.cryptomorin", "ir.syrent.velocityvanish.dependencies.com.github.cryptomorin")
        relocate("cloud.commandframework", "ir.syrent.velocityvanish.dependencies.cloud.commandframework")
        relocate("kotlin", "ir.syrent.velocityvanish.dependencies.kotlin")
        relocate("com.jeff_media", "ir.syrent.velocityvanish.dependencies.com.jeff_media")
        relocate("org.jetbrains", "ir.syrent.velocityvanish.dependencies.org.jetbrains")
        relocate("org.intellij", "ir.syrent.velocityvanish.dependencies.org.intellij")
    }

    build {
        dependsOn(clean)
        dependsOn(shadowJar)
    }

    jar {
        enabled = true
    }

    publishAllPublicationsToHangar {
        this.dependsOn(shadowJar)
        this.mustRunAfter(shadowJar)
    }

    withType<KotlinCompile> {
        dependsOn(generateNmsComponents)
        kotlinOptions.jvmTarget = "17"
    }
}


java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

artifacts.archives(tasks.shadowJar)

hangarPublish {
    publications.register("plugin") {
        version.set(suffixedVersion)
        channel.set(System.getenv("HANGAR_BUILD_CHANNEL") ?: "Snapshot")
        changelog.set(if (System.getenv("HANGAR_CHANGELOG").isNullOrEmpty()) changelogContent else System.getenv("HANGAR_CHANGELOG"))
        id.set(slug)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set((property("paperVersion") as String).split(",").map { it.trim() })
            }

            register(Platforms.VELOCITY) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set((property("velocityVersion") as String).split(",").map { it.trim() })
            }
        }
    }
}

modrinth {
    val modrinthApiKey = System.getenv("MODRINTH_API_TOKEN")
    val modrinthChangelog = if (System.getenv("MODRINTH_CHANGELOG").isNullOrEmpty()) changelogContent else System.getenv("MODRINTH_CHANGELOG")

    token.set(modrinthApiKey)
    projectId.set("${property("modrinthProjectID")}")
    versionNumber.set(suffixedVersion)
    versionType.set(System.getenv("MODRINTH_BUILD_CHANNEL") ?: "beta")
    uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    gameVersions.set("${property("modrinthMinecraftVersions")}".split(","))
    loaders.set("${property("modrinthLoaders")}".split(","))

    changelog.set(modrinthChangelog)

    syncBodyFrom.set(rootProject.file("README.md").readText())
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf(
        "version" to project.version
    )
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.get().java.srcDir(generateTemplates.map { outputs -> outputs })
rootProject.idea.project.settings.taskTriggers.afterSync(generateTemplates)

/* First add a new source set. Don't use your main source set for generated stuff. */
sourceSets.main.get().java.srcDirs("src/generated/java", "src/main/java")

/* All other things will be set inside the nmsGen method, */
nmsGen {
    basePackage = "ir.syrent.nms.accessors" // All generated classes will be in this package.
    sourceSet = "src/generated/java" // All generated classes will be part of this source set.
    minMinecraftVersion = "1.8"

    /* This means that the folder will be cleared before generation.
     *
     * If this value is false, old and no longer used classes won't be removed.
     */
    isCleanOnRebuild = true

    /* Here we can define the classes */
    val ServerGamePacketListenerImpl = reqClass("net.minecraft.server.network.ServerGamePacketListenerImpl")
    val ClientboundUpdateMobEffectPacket = reqClass("net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket")
    val ClientboundRemoveMobEffectPacket = reqClass("net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket")
    val ClientboundPlayerInfoUpdatePacket = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket")
    val ClientboundPlayerInfoUpdatePacketAction = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket\$Action")
    val ClientboundPlayerInfoUpdatePacketEntry = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket\$Entry")
    val ClientboundPlayerInfoRemovePacket = reqClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket")

    val ServerPlayer = reqClass("net.minecraft.server.level.ServerPlayer")
    val Entity = reqClass("net.minecraft.world.entity.Entity")
    val GameType = reqClass("net.minecraft.world.level.GameType")
    val Component = reqClass("net.minecraft.network.chat.Component")
    val RemoteChatSessionData = reqClass("net.minecraft.network.chat.RemoteChatSession\$Data")
    val Packet = reqClass("net.minecraft.network.protocol.Packet")
    val Connection = reqClass("net.minecraft.network.Connection")


    val MobEffect = reqClass("net.minecraft.world.effect.MobEffect")
    val MobEffectInstance = reqClass("net.minecraft.world.effect.MobEffectInstance")

    Connection
        .reqField("channel")

    ServerPlayer
        .reqField("connection")
    Entity
        .reqMethod("getUUID")
    GameType
        .reqEnumField("SURVIVAL")
        .reqEnumField("CREATIVE")
        .reqEnumField("SPECTATOR")
        .reqEnumField("ADVENTURE")
        .reqMethod("byName", String::class)
    MobEffect
        .reqMethod("byId", Int::class)
    MobEffectInstance
        .reqConstructor(MobEffect, Int::class, Int::class, Boolean::class, Boolean::class, Boolean::class)
        .reqField("effect")

    ServerGamePacketListenerImpl
        .reqMethod("send", Packet)
        .reqField("connection")
    ClientboundUpdateMobEffectPacket
        .reqConstructor(Int::class, MobEffectInstance)
    ClientboundRemoveMobEffectPacket
        .reqConstructor(Int::class, MobEffect)
    ClientboundPlayerInfoUpdatePacket
        .reqConstructor(ClientboundPlayerInfoUpdatePacketAction, ServerPlayer)
        .reqMethod("createPlayerInitializing", Collection::class)
        .reqField("entries")
        .reqMethod("entries")
    ClientboundPlayerInfoUpdatePacketAction
        .reqEnumField("UPDATE_GAME_MODE")
        .reqEnumField("ADD_PLAYER")
    ClientboundPlayerInfoUpdatePacketEntry
        .reqConstructor(UUID::class, "com.mojang.authlib.GameProfile", Boolean::class, Int::class, GameType, Component, RemoteChatSessionData)
    ClientboundPlayerInfoRemovePacket
        .reqConstructor(List::class)
        .reqField("profileIds")
}