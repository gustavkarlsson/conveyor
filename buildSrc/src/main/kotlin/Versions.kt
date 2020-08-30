import org.gradle.api.JavaVersion

object Versions {
    // Config
    val java = JavaVersion.VERSION_1_8
    val kotlin = "1.4.0"
    // Skip 1.11.0 as it has breaking bug: https://github.com/allegro/axion-release-plugin/issues/332
    // Skip 1.12.0 as it has breaking bug: https://github.com/allegro/axion-release-plugin/issues/339
    val ktlint = "9.3.0"

    // Dependencies
    val kotlinxCoroutines = "1.3.9"

    // Test
    val junit5 = "5.6.2"
    val spek = "2.0.12"
    val strikt = "0.27.0"
    val mockk = "1.10.0"
}
