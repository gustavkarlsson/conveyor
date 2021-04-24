package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.AtomicStateFlow

internal class StateManager<State> private constructor(
    private val incomingMutableState: StatefulMutableSharedFlow<State>,
    private val transformers: Iterable<Transformer<State>>,
) : StateFlow<State> by incomingMutableState, AtomicStateFlow<State>, Launcher {
    constructor(
        initialValue: State,
        transformers: Iterable<Transformer<State>>,
    ) : this(StatefulMutableSharedFlow(initialValue), transformers)

    private val outgoingMutableState = StatefulMutableSharedFlow(incomingMutableState.value)
    val outgoingState: StateFlow<State> = outgoingMutableState

    override fun launch(scope: CoroutineScope): Job = scope.launch {
        incomingMutableState
            .transform(transformers)
            .collect { outgoingMutableState.emit(it) }
    }

    private val writeMutex = Mutex()

    override suspend fun update(block: State.() -> State): State {
        return writeMutex.withLock {
            val newState = value.block()
            incomingMutableState.emit(newState)
            newState
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
