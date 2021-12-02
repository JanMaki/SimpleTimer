import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "net.necromagic.simpletimerKT"
version = "1.5.5"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://m2.dv8tion.net/releases")
}

dependencies {

    implementation(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

    implementation("net.dv8tion", "JDA", "5.0.0-alpha.1")
    //implementation(files("libs/JDA-4.3.0_DEV-withDependencies.jar"))

    implementation("me.carleslc.Simple-YAML", "Simple-Yaml", "1.7.2")

    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.0-RC")
    implementation("com.github.kittinunf.fuel", "fuel", "2.3.1")

    implementation("org.slf4j", "slf4j-simple", "1.7.30")
    implementation("org.slf4j", "slf4j-api", "1.7.30")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "net.necromagic.simpletimerKT.SimpleTimer"
    }

    from(
        configurations.compile.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}