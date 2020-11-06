import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version Versions.compose
}

repositories {
    jcenter()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
}
/*
TODO: This might be necessary to run from command line

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "11"
        useIR = true
    }
}
*/
compose.desktop {
    application {
        mainClass = "se.gustavkarlsson.conveyor.demo.MainKt"
    }
}
