package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SuspendingMutableStateFlow<T>
private constructor(
    private val inner: MutableSharedFlow<T>,
    initialValue: T,
) : MutableSharedFlow<T> by inner, StateFlow<T> {
    constructor(initialValue: T) : this(MutableSharedFlow(replay = 1), initialValue)

    init {
        check(inner.tryEmit(initialValue)) { "Initial value rejected" }
    }

    private val writeMutex = Mutex()

    override suspend fun emit(value: T) {
        writeMutex.withLock {
            inner.emit(value)
            this.value = value
        }
    }

    // FIXME test
    override fun tryEmit(value: T): Boolean {
        if (!writeMutex.tryLock()) return false
        val emitted = inner.tryEmit(value)
        if (emitted) {
            this.value = value
        }
        writeMutex.unlock()
        return emitted
    }

    override var value: T = initialValue
        private set
}
