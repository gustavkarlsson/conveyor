package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.StateUpdateException
import se.gustavkarlsson.conveyor.StoreFlow

internal class StateManager<State> private constructor(
    private val incomingMutableState: StatefulMutableSharedFlow<State>,
    private val transformers: Iterable<Transformer<State>>,
) : StateFlow<State> by incomingMutableState, StoreFlow<State>, Process {
    constructor(
        initialValue: State,
        transformers: Iterable<Transformer<State>>,
    ) : this(StatefulMutableSharedFlow(initialValue), transformers)

    private val outgoingMutableState = StatefulMutableSharedFlow(incomingMutableState.value)
    val outgoingState: StateFlow<State> = outgoingMutableState

    override suspend fun run() {
        incomingMutableState
            .transform(transformers)
            .collect { outgoingMutableState.emit(it) }
    }

    private val writeMutex = Mutex()

    override suspend fun update(block: State.() -> State) {
        return writeMutex.withLock {
            val newState = value.updateWithExceptionHandling(block)
            incomingMutableState.emit(newState)
        }
    }

    override suspend fun updateAndGet(block: State.() -> State): State {
        return writeMutex.withLock {
            val newState = value.updateWithExceptionHandling(block)
            incomingMutableState.emit(newState)
            newState
        }
    }

    override suspend fun getAndUpdate(block: State.() -> State): State {
        return writeMutex.withLock {
            val oldState = value
            val newState = oldState.updateWithExceptionHandling(block)
            incomingMutableState.emit(newState)
            oldState
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun State.updateWithExceptionHandling(block: State.() -> State): State {
        return try {
            block()
        } catch (t: Throwable) {
            throw StateUpdateException(this, t)
        }
    }

    override suspend fun emit(value: State) {
        writeMutex.withLock {
            incomingMutableState.emit(value)
        }
    }

    override fun tryEmit(value: State): Boolean {
        if (!writeMutex.tryLock()) return false
        val emitted = incomingMutableState.tryEmit(value)
        writeMutex.unlock()
        return emitted
    }

    override val subscriptionCount: StateFlow<Int> by incomingMutableState::subscriptionCount

    override val storeSubscriberCount: StateFlow<Int> by outgoingMutableState::subscriptionCount
}
