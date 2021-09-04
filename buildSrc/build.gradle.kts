plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.kotlinBinaryCompatibilityValidatorGradlePlugin)
    implementation(libs.dokkaGradlePlugin)
    // FIXME duplicated versions in buildSrc/src/main/kotlin/Versions.kt
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.18.1")
}
