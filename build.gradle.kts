// กำหนดเวอร์ชันของไลบรารีต่างๆ ไว้ที่นี่เพื่อความสะดวกในการอัปเดต
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

// ปลั๊กอินที่จำเป็นสำหรับโปรเจกต์ Kotlin และ Ktor
plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.11"
    kotlin("plugin.serialization") version "1.9.23"
}

// ข้อมูลพื้นฐานของโปรเจกต์
group = "com.example"
version = "0.0.1"

// กำหนดค่าสำหรับแอปพลิเคชัน Ktor
application {
    mainClass.set("com.example.ApplicationKt")
}

// ที่เก็บ Dependencies (ส่วนใหญ่ใช้ mavenCentral ก็เพียงพอ)
repositories {
    mavenCentral()
}

// Dependencies ของโปรเจกต์: ไลบรารีที่จำเป็นทั้งหมด
dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // Ktor for JSON Serialization using kotlinx.serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}