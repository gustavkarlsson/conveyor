plugins {
    kotlin("jvm") version Versions.kotlin apply false
    id("org.jetbrains.dokka") version Versions.dokka apply false
    id("io.gitlab.arturbosch.detekt") version Versions.detekt apply false
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}
