// Stolen from https://github.com/Kotlin/kotlinx-io/blob/c5bba0114e33c2afd8e4b1ab214aad94ee416d00/core/jvmMain/src/kotlinx/io/JvmCloseable.kt
@file:JvmName("JvmCloseable")

package se.gustavkarlsson.conveyor.plugin.vcr

import java.lang.reflect.Method

public actual typealias Closeable = java.io.Closeable

internal actual fun Throwable.addSuppressedInternal(other: Throwable) {
    AddSuppressedMethod?.invoke(this, other)
}

private val AddSuppressedMethod: Method? by lazy {
    try {
        Throwable::class.java.getMethod("addSuppressed", Throwable::class.java)
    } catch (t: Throwable) {
        null
    }
}
