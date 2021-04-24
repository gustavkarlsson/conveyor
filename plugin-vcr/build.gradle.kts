import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jetbrains.dokka")
    id("io.gitlab.arturbosch.detekt")
    jacoco
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
            artifact(tasks["kotlinSourcesJar"])
            artifact(tasks["javadocJar"])
            from(components["java"])
        }
    }
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
    api(project(":core"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    testImplementation(project(":testing"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${Versions.spek}")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")

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
    kotlinOptions.jvmTarget = Versions.java
    if (name == "compileTestKotlin") {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.FlowPreview"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

jacoco {
    toolVersion = Versions.jacoco
}
