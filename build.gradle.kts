import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.10"

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.6.1")

    //Discord連携 JDA
    implementation("net.dv8tion", "JDA", "5.0.0-alpha.13")
    //implementation(files("libs/JDA-5.0.0-alpha.9_DEV-withDependencies.jar"))
    //JDAに必要なもの
    implementation("ch.qos.logback", "logback-classic", "1.2.8")
    //音再生
    implementation("com.sedmelluq", "lavaplayer", "1.3.78")
    implementation("com.github.aikaterna", "lavaplayer-natives", "original-SNAPSHOT")

    //YAML
    implementation("com.charleskorn.kaml", "kaml", "0.43.0")

    //Json
    implementation("org.jetbrains.kotlinx","kotlinx-serialization-json", "1.3.2")

    //BCDice
    implementation("dev.simpletimer", "bcdice-kt", "1.1.0")

    //RESTApi
    implementation("com.github.kittinunf.fuel", "fuel", "2.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "dev.simpletimer.SimpleTimer"
    }

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}