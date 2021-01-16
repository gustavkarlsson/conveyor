plugins {
    kotlin("multiplatform") version Versions.kotlin apply false
    kotlin("jvm") version Versions.kotlin apply false
    id("io.gitlab.arturbosch.detekt") version Versions.detekt apply false
    id("org.jetbrains.dokka") version Versions.dokka
}

repositories {
    mavenCentral()
    jcenter()
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}
