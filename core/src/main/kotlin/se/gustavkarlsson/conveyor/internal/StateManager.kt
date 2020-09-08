package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(initialState: State) :
    ReadableStateContainer<State>,
    WriteableStateContainer<State>,
    Cancellable {
    private val channel = ConflatedBroadcastChannel(initialState)

    private val channelFlow = channel.asFlow().distinctUntilChanged { old, new -> old === new }

    override val state: Flow<State>
        get() = if (channel.isClosedForSend) {
            flowOf(currentState)
        } else {
            channelFlow
        }

    override var currentState: State = initialState
        set(value) {
            channel.offerOrThrow(value)
            field = value
        }

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
