plugins {
    common
    published
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxCoroutines}")
            }
        }
        val jvmTest by getting {
            dependencies {
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit5}")
                runtimeOnly("org.spekframework.spek2:spek-runner-junit5:${Versions.spek}")
                runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
                implementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit5}")
                implementation("org.spekframework.spek2:spek-dsl-jvm:${Versions.spek}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinxCoroutines}")
                implementation("io.strikt:strikt-core:${Versions.strikt}")
            }
        }
    }
}
