plugins {
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
}

apiValidation {
    ignoredProjects.addAll(listOf("plugin-vcr"))
}
