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
        val jvmTest by getting {
            dependencies {
                implementation(project(":test"))
                runtimeOnly(libs.bundles.testRuntime)
                implementation(libs.bundles.testImplementation)
            }
        }
    }
}
