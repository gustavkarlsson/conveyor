package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(initialState: State) :
    ReadableStateContainer<State>,
    WriteableStateContainer<State>,
    Cancellable {
    private val channel = ConflatedBroadcastChannel(initialState)

    override val state: Flow<State> =
        channel.asFlow()
            .distinctUntilChanged { old, new -> old === new }

    override var currentState: State
        get() = channel.value
        set(value) {
            check(channel.offer(value)) {
                "Failed to set state, channel over capacity"
            }
        }

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
