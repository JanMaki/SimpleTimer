import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    //Discord連携 JDA
    implementation("net.dv8tion", "JDA", "5.0.0-beta.15")
    //JDAに必要なもの
    implementation("ch.qos.logback", "logback-classic", "1.2.8")
    //音再生
    implementation("com.github.walkyst", "lavaplayer-fork", "1.4.3")
    implementation("com.github.Walkyst", "lavaplayer-natives-fork", "1.0.2")

    //YAML
    implementation("com.charleskorn.kaml", "kaml", "0.55.0")
    //Json
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.6.0")

    //BCDice
    implementation("dev.simpletimer", "bcdice-kt", "1.6.1")

    //RESTApi
    implementation("com.github.kittinunf.fuel", "fuel", "2.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
}

val jar by tasks.getting(Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "dev.simpletimer.SimpleTimerKt"
    }

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}