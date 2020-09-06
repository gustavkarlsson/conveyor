package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

@ExperimentalCoroutinesApi
internal class CommandManager<State>(
    bufferSize: Int,
    private val getState: () -> State,
    private val setState: (State) -> Unit,
) : CommandIssuer<State>, CommandProcessor<State> {
    init {
        require(bufferSize > 0) {
            "bufferSize must be positive. Was: $bufferSize"
        }
    }

    private val channel = Channel<Command<State>>(bufferSize)

    override suspend fun issue(command: Command<State>) = channel.send(command)

    override suspend fun process(scope: CoroutineScope) =
        channel.consumeEach { command ->
            val oldState = getState()
            val (newState, actions) = command.reduce(oldState)
            setState(newState)
            for (action in actions) {
                scope.launch { action.execute(this@CommandManager) }
            }
        }

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
