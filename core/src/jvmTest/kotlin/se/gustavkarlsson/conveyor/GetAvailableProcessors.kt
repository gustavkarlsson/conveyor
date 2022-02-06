package se.gustavkarlsson.conveyor

actual fun getAvailableProcessors(): Int {
    return Runtime.getRuntime().availableProcessors()
}
