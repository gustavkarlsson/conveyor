import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
}
