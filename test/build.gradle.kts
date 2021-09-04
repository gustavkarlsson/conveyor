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
                api(libs.kotlinCoroutines.core)
            }
        }
        val jvmTest by getting {
            dependencies {
                runtimeOnly(libs.bundles.testRuntime)
                implementation(libs.bundles.testImplementation)
            }
        }
    }
}
