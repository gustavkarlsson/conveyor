package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateHolderImpl<State>(initialState: State) : StateHolder<State> {
    private val channel = ConflatedBroadcastChannel(initialState)

    override fun get(): State = channel.value

    override fun set(state: State) {
        check(channel.offer(state)) {
            "Failed to set state, channel over capacity"
        }
    }

    override val flow: Flow<State> =
        channel.asFlow()
            .distinctUntilChanged { old, new -> old === new }

    override fun close(cause: Throwable?) {
        channel.close(cause)
    }
}
