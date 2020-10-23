package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import se.gustavkarlsson.conveyor.Action

@ExperimentalCoroutinesApi
internal class ManualActionsManager<State> : ActionIssuer<State>, ActionFlowProvider<State>, Cancellable {
    private val channel = Channel<Action<State>>(Channel.UNLIMITED)

    override fun issue(action: Action<State>) = channel.offerOrThrow(action)

    override val actionFlow: Flow<Action<State>> = channel.consumeAsFlow()

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
