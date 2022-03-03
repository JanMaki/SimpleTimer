import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.4.20"

    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "dev.simpletimer"
version = "1.5.5"
val name = "SimpleTimer"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    //Discord連携 JDA
    implementation("net.dv8tion", "JDA", "5.0.0-alpha.8")
    //implementation(files("libs/JDA-4.3.0_DEV-withDependencies.jar"))
    implementation("org.slf4j", "slf4j-simple", "1.7.30")
    implementation("org.slf4j", "slf4j-api", "1.7.30")

    //YAML
    implementation("com.charleskorn.kaml","kaml","0.42.0")

    //BCDice
    implementation("dev.simpletimer","bcdice-kt","1.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "dev.simpletimer.SimpleTimer"
    }

    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}