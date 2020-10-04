package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import se.gustavkarlsson.conveyor.Action

@ExperimentalCoroutinesApi
internal class ManualActionsManager<State> : ActionIssuer<State>, Processor<State>, Cancellable {
    private val channel = Channel<Action<State>>(Channel.UNLIMITED)

    override fun issue(action: Action<State>) = channel.offerOrThrow(action)

    override suspend fun process(onAction: suspend (Action<State>) -> Unit) =
        channel.consumeEach { action ->
            onAction(action)
        }

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
