plugins {
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
    jcenter {
        content {
            includeModule("org.jetbrains.kotlinx", "kotlinx-html-jvm") // Remove after upgrading detekt to 1.17
        }
    }
}
