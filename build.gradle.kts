plugins {
    kotlin("jvm") version "1.9.22"

    kotlin("plugin.serialization") version "1.9.10"
    application
    id("io.ktor.plugin") version "2.3.4"
}

group = "com.demo.app"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.demo.app.ApplicationKt") // or your main class
}

repositories {
    mavenCentral()
}



dependencies {

    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-core-jvm:2.3.4")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.insert-koin:koin-ktor:3.5.0")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    // Database Driver — e.g. H2 for demo, or PostgreSQL
    implementation("com.h2database:h2:2.2.224")
    // or PostgreSQL: implementation("org.postgresql:postgresql:42.7.3")

    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.4")
    testImplementation("io.insert-koin:koin-test:3.5.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
    testImplementation("io.insert-koin:koin-test-junit5:3.5.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }

    testImplementation("io.mockk:mockk:1.13.10")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")




}

tasks.test {
    useJUnitPlatform()
}



kotlin {
    jvmToolchain(21)
}