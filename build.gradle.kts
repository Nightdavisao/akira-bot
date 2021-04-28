plugins {
    kotlin("jvm") version "1.4.30"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.github.nightdavisao"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.perfectdreams.net/")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("akira-bot")

        //mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "com.github.nightdavisao.akirabot.AkiraLauncher"))
        }
    }

    task("stage") {
        dependsOn("shadowJar", "clean")
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("dev.kord:kord-core:0.7.x-SNAPSHOT")
    implementation("net.perfectdreams.discordinteraktions:core:0.0.4-SNAPSHOT")
    implementation("net.perfectdreams.discordinteraktions:gateway-kord:0.0.4-SNAPSHOT")

    // https://github.com/mwanji/toml4j
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    // https://github.com/mmastrac/nanojson
    implementation("com.grack:nanojson:1.7")

    implementation("io.ktor:ktor-client-core:1.5.2")
    implementation("io.ktor:ktor-client-cio:1.5.2")

    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")
    implementation("org.slf4j:slf4j-simple:1.7.29")

    val exposedVersion: String by project
    implementation("com.h2database:h2:1.4.200")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
}
