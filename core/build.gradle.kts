plugins {
    kotlin("multiplatform")
    // java is required for jacoco according to
    // https://nwillc.medium.com/kotlin-multiplatform-first-contact-bintray-jacoco-part-3-dbd496bf168a
    java
    `maven-publish`
    id("org.jetbrains.dokka")
    id("io.gitlab.arturbosch.detekt")
    jacoco
}

repositories {
    mavenCentral()
    jcenter {
        content {
            includeModule("org.jetbrains.kotlinx", "kotlinx-html-jvm")
        }
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}")
}

kotlin {
    explicitApi()
    targets.all {
        compilations.all {
            if (name.contains("test", ignoreCase = true)) {
                kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.FlowPreview"
                kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            }
        }
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = Versions.java
        }
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
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxCoroutines}")
            }
        }
        val jvmMain by getting {}
        val jsMain by getting {}
        val jvmTest by getting {
            dependencies {
                implementation(project(":testing"))
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}")
                runtimeOnly("org.spekframework.spek2:spek-runner-junit5:${Versions.spek}")
                runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
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
        xml.isEnabled = true
        html.isEnabled = true
    }
}

jacoco {
    toolVersion = Versions.jacoco
}

detekt {
    input = files(fileTree("src").include("*/kotlin/"))
}
