plugins {
    kotlin("multiplatform") version Versions.kotlin apply false
    kotlin("jvm") version Versions.kotlin apply false
    id("io.gitlab.arturbosch.detekt") version Versions.detekt apply false
    id("org.jetbrains.dokka") version Versions.dokka
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version Versions.binaryCompatibilityValidator
}

repositories {
    mavenCentral()
    jcenter {
        content {
            includeModule("org.jetbrains.kotlinx", "kotlinx-html-jvm")
        }
    }
}

apiValidation {
    ignoredProjects.addAll(listOf("testing", "demo"))
}
