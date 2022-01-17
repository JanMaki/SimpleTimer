import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "net.necromagic.simpletimer"
version = "1.5.5"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://m2.dv8tion.net/releases")
}

dependencies {

    implementation(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

    //Discord連携 JDA
    implementation("net.dv8tion", "JDA", "5.0.0-alpha.2")
    //implementation(files("libs/JDA-4.3.0_DEV-withDependencies.jar"))
    implementation("org.slf4j", "slf4j-simple", "1.7.30")
    implementation("org.slf4j", "slf4j-api", "1.7.30")

    //YAML
    implementation("me.carleslc.Simple-YAML", "Simple-Yaml", "1.7.2")

    //BCDice
    implementation("dev.simpletimer","bcdice-kt","alpha-1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "net.necromagic.simpletimer.SimpleTimer"
    }

    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}