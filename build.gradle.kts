plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "1.1.0"
}

group = "com.kruthers"
version = "2.2.0"
description = "Allows you to ingrate github support into your datapack folder in game"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(kotlin("stdlib")) //kotlin is provided by panda lib
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    //jagit
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.3.0.202209071007-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:6.3.0.202209071007-r")

    //cloud - provided by panda lib
    val cloudVersion = project.properties["cloud_version"].toString()
    compileOnly("cloud.commandframework","cloud-core",cloudVersion)
    compileOnly("cloud.commandframework","cloud-annotations",cloudVersion)
    compileOnly("cloud.commandframework","cloud-paper",cloudVersion)
    compileOnly("cloud.commandframework","cloud-minecraft-extras",cloudVersion)
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        relocate("cloud","com.kruthers.pandalib.cloud")
//        relocate("kotlin","com.kruthers.pandalib.kotlin")

        minimize {
            exclude(dependency("org.eclipse.jgit:org.eclipse.jgit:.*"))
        }
        minimize()
    }
    build {
        dependsOn(shadowJar)
        dependsOn(processResources)
    }
    processResources {
        expand("name" to project.name, "description" to project.description, "version" to project.version)
    }
    runServer {
        dependsOn(build)
        minecraftVersion("1.20.1")
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}