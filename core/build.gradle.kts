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
            }
        }
        val jvmTest by getting {
            dependencies {
                runtimeOnly(libs.bundles.testRuntime)
                implementation(libs.bundles.testImplementation)
                implementation(libs.kotest) // TODO kotest has multiplatform support. Try to enable it after removing spek
            }
        }
        val jsTest by getting
    }
}
