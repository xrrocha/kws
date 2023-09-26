plugins {
    kotlin("jvm") version "1.9.10"
    application
}

group = "ksw"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.0-M11")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper:11.0.0-M11")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")

    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:2.2.222")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.12.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    // jvmToolchain(17)
}

application {
    mainClass.set("kws.MainKt")
}
