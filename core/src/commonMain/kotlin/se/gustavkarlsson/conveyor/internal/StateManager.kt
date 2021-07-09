package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import se.gustavkarlsson.conveyor.AtomicStateFlow
import se.gustavkarlsson.conveyor.LockHandler

internal class StateManager<State> private constructor(
    private val incomingMutableState: StatefulMutableSharedFlow<State>,
    private val transformers: Iterable<Transformer<State>>,
    private val lockHandler: LockHandler,
) : StateFlow<State> by incomingMutableState, AtomicStateFlow<State>, Process {
    constructor(
        initialValue: State,
        transformers: Iterable<Transformer<State>>,
    ) : this(StatefulMutableSharedFlow(initialValue), transformers, SimpleLockHandler)

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
        return mutateWithLockCheck {
            val newState = value.block()
            incomingMutableState.emit(newState)
            newState
        }
    }

    override suspend fun emit(value: State) {
        mutateWithLockCheck {
            incomingMutableState.emit(value)
            value
        }
    }

    private suspend fun mutateWithLockCheck(block: suspend () -> State): State {
        writeMutex.withLock {
            var retries = 0
            var timeoutMillis = lockHandler.initialTimeoutMillis
            while (true) {
                val result = withTimeoutOrNull(timeoutMillis) {
                    block()
                }
                if (result != null) {
                    return result
                }
                when (val resolution = lockHandler.onLock(retries++)) {
                    is LockHandler.Resolution.Retry -> {
                        timeoutMillis = resolution.timeoutMillis
                    }
                    is LockHandler.Resolution.Throw -> {
                        throw resolution.exception
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

private object SimpleLockHandler : LockHandler {
    override val initialTimeoutMillis: Long = 1000

    override fun onLock(retries: Int): LockHandler.Resolution {
        return if (retries > 3) {
            LockHandler.Resolution.Throw(Exception("Locked: Giving up!"))
        } else {
            val retryInMillis = (retries + 1) * initialTimeoutMillis
            println("Locked ($retries): Retrying for ${retryInMillis}ms")
            LockHandler.Resolution.Retry(retryInMillis)
        }
    }
}
