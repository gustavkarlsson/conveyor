plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.dokka")
    id("io.kotest.multiplatform")
    // java is required for jacoco according to
    // https://nwillc.medium.com/kotlin-multiplatform-first-contact-bintray-jacoco-part-3-dbd496bf168a
    java
    `maven-publish`
    jacoco
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    targets {
        jvm {
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
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

tasks.jacocoTestReport {
    dependsOn("jvmTest")
    val sourceDirs = arrayOf("commonMain/src", "jvmMain/src")
    sourceDirectories.setFrom(sourceDirs)
    additionalSourceDirs.setFrom(sourceDirs)
    classDirectories.setFrom("${buildDir}/classes/kotlin/jvm/main/")
    executionData.setFrom("${buildDir}/jacoco/jvmTest.exec")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

apiValidation {
    nonPublicMarkers.add("se.gustavkarlsson.conveyor.InternalConveyorApi")
}
