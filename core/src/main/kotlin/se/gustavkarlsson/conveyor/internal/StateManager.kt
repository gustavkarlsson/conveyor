package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.gustavkarlsson.conveyor.StateAccess

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(
    initialState: State,
    scope: CoroutineScope,
) : StateAccess<State>, Cancellable {
    // TODO can currentState NOT be initialized before running transformers?
    private var internalState: State = initialState
    private val operationChannel = Channel<suspend (State) -> State>()
    private val stateChannel = ConflatedBroadcastChannel<State>()
        .apply { offerOrThrow(initialState) }

    // FIXME simplify now that transformers are not necessary?
    private val job = scope.launch {
        operationChannel.consumeAsFlow()
            .map { operation ->
                val oldState = internalState
                val newState = operation(oldState)
                internalState = newState
                newState
            }
            .distinctUntilChanged { old, new -> old === new }
            .onCompletion { cause ->
                stateChannel.close(cause)
            }
            .collect { state ->
                stateChannel.offerOrThrow(state)
            }
    }

    override val flow: Flow<State> = stateChannel.asFlow()
        .onEmpty { emit(get()) }

    override fun get(): State = runBlocking { flow.first() }

    override suspend fun set(state: State) {
        operationChannel.send { state }
    }

    override suspend fun update(block: suspend (State) -> State) {
        operationChannel.send { block(it) }
    }

    override fun cancel(cause: Throwable?) {
        job.cancel(cause as? CancellationException)
    }
}
