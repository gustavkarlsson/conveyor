plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

// FIXME duplicated versions in buildSrc/src/main/kotlin/Versions.kt
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20")
    implementation("org.jetbrains.kotlinx.binary-compatibility-validator:org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:0.5.0")
    implementation("org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin:1.4.32")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.17.1")
}
