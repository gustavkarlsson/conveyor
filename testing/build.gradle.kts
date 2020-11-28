import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    api(project(":core"))
    api("org.junit.jupiter:junit-jupiter-api:${Versions.junit5}")
    api("org.spekframework.spek2:spek-dsl-jvm:${Versions.spek}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinxCoroutines}")
    api("io.strikt:strikt-core:${Versions.strikt}")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.java
if (name == "compileKotlin") {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.FlowPreview"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}
