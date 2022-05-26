import kotlinx.kover.api.CoverageEngine

plugins {
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover") version "0.5.1"
}

repositories {
    mavenCentral()
}

kover {
    coverageEngine.set(CoverageEngine.JACOCO)
}
