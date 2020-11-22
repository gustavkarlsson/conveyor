package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import se.gustavkarlsson.conveyor.Action

internal class ActionIssuerImpl<State> : ActionIssuer<State> {
    private val actionChannel = Channel<Action<State>>(Channel.UNLIMITED)
    val issuedActions = actionChannel.consumeAsFlow()
    override fun issue(action: Action<State>) {
        actionChannel.offer(action)
    }

    override fun cancel(cause: Throwable?) = actionChannel.cancel(cause as? CancellationException)
}
