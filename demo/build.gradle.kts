import org.jetbrains.compose.compose

plugins {
    common
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.0.1"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(project(":core", "archives"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "se.gustavkarlsson.conveyor.demo.MainKt"
    }
}
