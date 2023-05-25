plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.kruthers"
version = "2.1.1"
description = "Allows you to ingrate github support into your datapack folder in game"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    shadow("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.3.0.202209071007-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:6.3.0.202209071007-r")


    implementation("net.kyori","adventure-platform-bukkit","4.3.0")


    implementation("cloud.commandframework","cloud-core","1.8.3")
    implementation("cloud.commandframework","cloud-annotations","1.8.3")
    implementation("cloud.commandframework","cloud-paper","1.8.3")
    implementation("cloud.commandframework","cloud-minecraft-extras","1.8.3")
}

tasks {
    shadowJar {
        destinationDirectory.set(file("build"))
        archiveClassifier.set("")

        minimize {
            exclude(dependency("org.eclipse.jgit:org.eclipse.jgit:.*"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
    processResources {
        expand("name" to project.name, "description" to project.description, "version" to project.version)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}