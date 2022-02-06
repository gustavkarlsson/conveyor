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
                implementation(libs.kotest.engine)
                implementation(libs.kotest.assertions)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.jvm)
            }
        }
        val jsTest by getting
    }
}
