package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.InternalConveyorApi

@InternalConveyorApi
public class StatefulMutableSharedFlow<T>
private constructor(
    private val inner: MutableSharedFlow<T>,
    initialValue: T,
) : MutableSharedFlow<T> by inner, StateFlow<T> {
    public constructor(initialValue: T) : this(MutableSharedFlow(replay = 1), initialValue)

    init {
        check(inner.tryEmit(initialValue)) { "Initial value rejected" }
    }

    private val writeMutex = Mutex()

    override suspend fun emit(value: T) {
        writeMutex.withLock {
            if (this.value != value) {
                inner.emit(value)
            }
        }
    }

    override fun tryEmit(value: T): Boolean {
        if (!writeMutex.tryLock()) return false
        val emitted = if (this.value != value) {
            inner.tryEmit(value)
        } else true
        writeMutex.unlock()
        return emitted
    }

    override val value: T get() = replayCache.first()
}
