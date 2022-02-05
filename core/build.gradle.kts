plugins {
    common
    published
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                api(libs.kotlinCoroutines.core)
            }
        }
        val jvmMain by getting
        val jsMain by getting
        val commonTest by getting {
            dependencies {
                implementation(project(":test", "archives"))
                implementation(libs.kotest.assertions)
                implementation("io.kotest:kotest-framework-engine:5.1.0") // FIXME catalog
            }
        }
        val jvmTest by getting {
            dependencies {
                runtimeOnly(libs.bundles.testRuntime)
                implementation(libs.bundles.testImplementation)
                implementation("io.kotest:kotest-runner-junit5-jvm:5.1.0")
            }
        }
        val jsTest by getting
    }
}
