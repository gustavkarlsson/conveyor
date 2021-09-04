package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.internal.StatefulMutableSharedFlow

public class TestStoreFlow<State> private constructor(
    private val inner: StatefulMutableSharedFlow<State>,
) : StateFlow<State> by inner, StoreFlow<State> {
    public constructor(initialValue: State) : this(StatefulMutableSharedFlow(initialValue))

    private val writeMutex = Mutex()

    override suspend fun update(block: State.() -> State) {
        return writeMutex.withLock {
            val newState = value.block()
            inner.emit(newState)
        }
    }

    override suspend fun updateAndGet(block: State.() -> State): State {
        return writeMutex.withLock {
            val newState = value.block()
            inner.emit(newState)
            newState
        }
    }

    override suspend fun getAndUpdate(block: State.() -> State): State {
        return writeMutex.withLock {
            val oldState = value
            val newState = oldState.block()
            inner.emit(newState)
            oldState
        }
    }

    override suspend fun emit(value: State) {
        writeMutex.withLock {
            inner.emit(value)
        }
    }

    override fun tryEmit(value: State): Boolean {
        if (!writeMutex.tryLock()) return false
        val emitted = inner.tryEmit(value)
        writeMutex.unlock()
        return emitted
    }

    override val subscriptionCount: StateFlow<Int> by inner::subscriptionCount

    override val storeSubscriberCount: MutableStateFlow<Int> = MutableStateFlow(0)
}
