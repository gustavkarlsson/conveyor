package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlin.properties.Delegates.notNull

// TODO This is not that nice. Rework?
internal class TrackPosition(
    private val getCurrent: () -> Long,
) {
    private var last by notNull<Long>()

    fun start() {
        last = getCurrent()
    }

    @Synchronized
    fun getDelta(): Long {
        val current = getCurrent()
        val delta = current - last
        last = current
        return delta
    }
}
