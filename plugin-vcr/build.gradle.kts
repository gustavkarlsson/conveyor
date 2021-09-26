plugins {
    common
    published
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                project(":core")
                implementation(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                api(libs.kotlinCoroutines.core)
            }
        }
    }
}
