package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.UpdatableStateFlow

internal class StateManager<State> private constructor(
    private val incomingMutableState: SuspendingMutableStateFlow<State>,
    private val transformers: Iterable<Transformer<State>>,
) : StateFlow<State> by incomingMutableState, UpdatableStateFlow<State>, Launcher {
    constructor(
        initialValue: State,
        transformers: Iterable<Transformer<State>>,
    ) : this(SuspendingMutableStateFlow(initialValue), transformers)

    private val outgoingMutableState = SuspendingMutableStateFlow(incomingMutableState.value)
    val outgoingState: StateFlow<State> = outgoingMutableState

    override fun launch(scope: CoroutineScope): Job = scope.launch {
        incomingMutableState
            .transform(transformers)
            .collect { outgoingMutableState.emit(it) }
    }

    private val writeMutex = Mutex()
    override suspend fun update(block: suspend State.() -> State): State =
        writeMutex.withLock {
            val newState = value.block()
            incomingMutableState.emit(newState)
            newState
        }

    override val storeSubscriberCount: StateFlow<Int> by outgoingMutableState::subscriptionCount
}