// Stolen from [https://github.com/Kotlin/kotlinx-io/blob/7f1670e003ecfb74e75adddc28c5ab487c1974f9/core/commonMain/src/kotlinx/io/Closeable.common.kt]
package se.gustavkarlsson.conveyor.plugin.vcr

/**
 * Closeable resource.
 */
public expect interface Closeable {
    public fun close()
}

/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * @param block a function to process this [Closeable] resource.
 * @return the result of [block] function invoked on this resource.
 */
internal inline fun <C : Closeable, R> C.use(block: (C) -> R): R {
    var closed = false

    return try {
        block(this)
    } catch (first: Throwable) {
        try {
            closed = true
            close()
        } catch (second: Throwable) {
            first.addSuppressedInternal(second)
        }

        throw first
    } finally {
        if (!closed) {
            close()
        }
    }
}

internal expect fun Throwable.addSuppressedInternal(other: Throwable)
