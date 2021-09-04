plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinGradle)
    implementation(libs.kotlinBinaryCompatibilityValidatorGradle)
    implementation(libs.dokkaGradle)
    // FIXME duplicated versions in buildSrc/src/main/kotlin/Versions.kt
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.18.1")
}
