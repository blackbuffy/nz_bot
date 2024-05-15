plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.ghwu"
version = "0.1k-test.20"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.23")
    implementation("mysql:mysql-connector-java:8.0.29")
    implementation("org.json:json:20210307")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-Beta")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}