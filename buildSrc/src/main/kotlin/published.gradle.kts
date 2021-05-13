plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.dokka")
    // java is required for jacoco according to
    // https://nwillc.medium.com/kotlin-multiplatform-first-contact-bintray-jacoco-part-3-dbd496bf168a
    java
    `maven-publish`
    jacoco
}

repositories {
    mavenCentral()
    jcenter {
        content {
            includeModule("org.jetbrains.kotlinx", "kotlinx-html-jvm") // Remove after upgrading detekt to 1.17
        }
    }
}

kotlin {
    explicitApi()
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.useExperimentalAnnotation("se.gustavkarlsson.conveyor.InternalConveyorApi")
            if (name.contains("test", ignoreCase = true)) {
                languageSettings.useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
                languageSettings.useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform {
                includeEngines("spek2")
            }
        }
    }
    js(IR) {
        browser()
        nodejs()
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
        xml.isEnabled = true
        html.isEnabled = true
    }
}

jacoco {
    toolVersion = Versions.jacoco
}

apiValidation {
    nonPublicMarkers.add("se.gustavkarlsson.conveyor.InternalConveyorApi")
}
