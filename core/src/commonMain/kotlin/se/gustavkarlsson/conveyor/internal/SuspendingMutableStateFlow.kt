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

    // FIXME Write test to ensure distinct

    override suspend fun emit(value: T) {
        writeMutex.withLock {
            if (this.value != value) {
                inner.emit(value)
                this.value = value
            }
        }
    }

    override fun tryEmit(value: T): Boolean {
        if (!writeMutex.tryLock()) return false
        val emitted = if (this.value != value) {
            inner.tryEmit(value)
        } else true
        this.value = value
        writeMutex.unlock()
        return emitted
    }

    override var value: T = initialValue
        private set
}
