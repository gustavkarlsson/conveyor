package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.ActionIssuer

@ExperimentalCoroutinesApi
internal class ManualActionsManager<State>(
    bufferSize: Int,
) : ActionIssuer<State>, Processor<State>, Cancellable {
    init {
        require(bufferSize > 0) {
            "bufferSize must be positive. Was: $bufferSize"
        }
    }

    private val channel = Channel<Action<State>>(bufferSize)

    override fun issue(action: Action<State>) = channel.offerOrThrow(action)

    override suspend fun process(onAction: suspend (Action<State>) -> Unit) =
        channel.consumeEach { action ->
            onAction(action)
        }

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
