package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import se.gustavkarlsson.conveyor.AtomicStateFlow
import se.gustavkarlsson.conveyor.DeadlockHandler

internal class StateManager<State> private constructor(
    private val incomingMutableState: StatefulMutableSharedFlow<State>,
    private val transformers: Iterable<Transformer<State>>,
    private val deadlockHandler: DeadlockHandler,
) : StateFlow<State> by incomingMutableState, AtomicStateFlow<State>, Process {
    constructor(
        initialValue: State,
        transformers: Iterable<Transformer<State>>,
    ) : this(StatefulMutableSharedFlow(initialValue), transformers, SimpleDeadlockHandler)

    private val outgoingMutableState = StatefulMutableSharedFlow(incomingMutableState.value)
    val outgoingState: StateFlow<State> = outgoingMutableState

    override suspend fun run() {
        incomingMutableState
            .transform(transformers)
            .collect {
                outgoingMutableState.emit(it)
            }
    }

    private val writeMutex = Mutex()

    override suspend fun update(block: State.() -> State): State {
        return mutateWithDeadlockCheck {
            val newState = value.block()
            incomingMutableState.emit(newState)
            newState
        }
    }

    override suspend fun emit(value: State) {
        mutateWithDeadlockCheck {
            incomingMutableState.emit(value)
            value
        }
    }

    private suspend fun mutateWithDeadlockCheck(block: suspend () -> State): State {
        writeMutex.withLock {
            var count = 0
            var timeoutMillis = deadlockHandler.initialTimeoutMillis
            while (true) {
                val result = withTimeoutOrNull(timeoutMillis) { block() }
                if (result != null) {
                    return result
                } else {
                    when (val resolution = deadlockHandler.onDeadlock(++count)) {
                        is DeadlockHandler.Resolution.Retry -> {
                            timeoutMillis = resolution.timeoutMillis
                        }
                        is DeadlockHandler.Resolution.Throw -> {
                            throw resolution.exception
                        }
                    }
                }
            }
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

// FIXME remove

private object SimpleDeadlockHandler : DeadlockHandler {
    override val initialTimeoutMillis: Long = 1000

    override fun onDeadlock(count: Int): DeadlockHandler.Resolution {
        return if (count > 3) {
            DeadlockHandler.Resolution.Throw(Exception("Deadlock: Gave up!"))
        } else {
            println("Deadlock: Retrying...")
            DeadlockHandler.Resolution.Retry(initialTimeoutMillis * count)
        }
    }
}
