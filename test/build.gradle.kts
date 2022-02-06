plugins {
    common
    published
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core", "archives"))
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                api(libs.kotlinCoroutines.core)
            }
        }
    }
}
