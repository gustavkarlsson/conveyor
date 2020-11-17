plugins {
    kotlin("jvm") version Versions.kotlin apply false
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}
