// Stolen from https://github.com/Kotlin/kotlinx-io/blob/c5bba0114e33c2afd8e4b1ab214aad94ee416d00/core/jsMain/src/kotlinx/io/JsCloseable.kt
package se.gustavkarlsson.conveyor.plugin.vcr

/**
 * Closeable resource.
 */
public actual interface Closeable {
    public actual fun close()
}

internal actual fun Throwable.addSuppressedInternal(other: Throwable) {
}
