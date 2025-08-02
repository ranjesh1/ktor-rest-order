plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.10"
    application
    id("io.ktor.plugin") version "2.3.4"
}

val kotlinVersion = "1.9.22"
val ktorVersion = "2.3.4"
val serializationVersion = "1.6.0"
val koinVersion = "3.5.0"
val logbackVersion = "1.4.11"
val exposedVersion = "0.50.1"
val h2Version = "2.2.224"
val junitVersion = "5.10.0"
val mockkVersion = "1.13.10"

group = "com.demo.app"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.demo.app.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor core & engine
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Koin integration
    implementation("io.insert-koin:koin-ktor:$koinVersion")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    // H2 Database (demo/test)
    implementation("com.h2database:h2:$h2Version")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
