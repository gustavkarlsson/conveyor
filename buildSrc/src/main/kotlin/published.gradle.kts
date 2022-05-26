plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.dokka")
    id("io.kotest.multiplatform")
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    targets {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
        }
        js(IR) {
            browser()
            nodejs()
        }
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("se.gustavkarlsson.conveyor.InternalConveyorApi")
            if (name.contains("test", ignoreCase = true)) {
                languageSettings.optIn("kotlinx.coroutines.FlowPreview")
                languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

task<Jar>("javadocJar") {
    from(tasks["dokkaJavadoc"].outputs)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "se.gustavkarlsson.conveyor"
            artifactId = "${rootProject.name}-${project.name}"
            version = "1.0-SNAPSHOT"
            artifact(tasks["jvmSourcesJar"])
            // artifact(tasks["javadocJar"]) TODO re-add once dokka supports multiplatform
            artifact(tasks["jvmJar"])
        }
    }
}

apiValidation {
    nonPublicMarkers.add("se.gustavkarlsson.conveyor.InternalConveyorApi")
}
