rootProject.name = "conveyor"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(
    "core",
    "test",
    "demo"
)
