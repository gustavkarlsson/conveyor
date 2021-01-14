package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SuspendingMutableStateFlow<T>
private constructor(
    private val inner: MutableSharedFlow<T>,
    initialValue: T,
) : SharedFlow<T> by inner, StateFlow<T> {
    constructor(
        initialValue: T,
        suspend: Boolean = true,
    ) : this(
        inner = MutableSharedFlow(
            replay = 1,
            extraBufferCapacity = 0,
            onBufferOverflow = if (suspend) BufferOverflow.SUSPEND else BufferOverflow.DROP_OLDEST,
        ),
        initialValue = initialValue,
    )

    private val mutex = Mutex()

    suspend fun emit(value: T) {
        mutex.withLock {
            inner.emit(value)
            this.value = value
        }
    }

    // FIXME add test for this
    val subscriptionCount: StateFlow<Int> by inner::subscriptionCount

    override var value: T = initialValue
        private set
}
