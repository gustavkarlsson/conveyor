import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlin
    id("io.gitlab.arturbosch.detekt") version Versions.detekt
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxCoroutines}")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinxCoroutines}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit5}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:${Versions.spek}")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Versions.spek}")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")

    testImplementation("io.strikt:strikt-core:${Versions.strikt}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}")
}

kotlin {
    explicitApi()
}

tasks.test {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = Versions.java.toString()
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    if (name == "compileTestKotlin") {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.FlowPreview"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}

group = "se.gustavkarlsson.conveyor"
version = "1.0-SNAPSHOT"