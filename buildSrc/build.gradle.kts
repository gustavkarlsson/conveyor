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
    implementation(libs.ktlintGradle)
}
