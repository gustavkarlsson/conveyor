package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

@ExperimentalCoroutinesApi
internal class CommandManager<State>(
    bufferSize: Int,
    private val stateContainer: WriteableStateContainer<State>,
) : CommandIssuer<State>, Processor<State>, Cancellable {
    init {
        require(bufferSize > 0) {
            "bufferSize must be positive. Was: $bufferSize"
        }
    }

    private val channel = Channel<Command<State>>(bufferSize)

    override suspend fun issue(command: Command<State>) = channel.send(command)

    override suspend fun process(onAction: suspend (Action<State>) -> Unit) =
        channel.consumeEach { command ->
            val oldState = stateContainer.currentState
            val (newState, actions) = command.reduce(oldState)
            stateContainer.currentState = newState
            for (action in actions) {
                onAction(action)
            }
        }

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
