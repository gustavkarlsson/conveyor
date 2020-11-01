package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess

@ExperimentalCoroutinesApi
internal class ManualActionsManager<State> : ActionIssuer<State>, ActionProcessor<State>, Cancellable {
    private val actionChannel = Channel<Action<State>>(Channel.UNLIMITED)
    private val actionFlow = actionChannel.consumeAsFlow()

    override fun issue(action: Action<State>) = actionChannel.offerOrThrow(action)

    override suspend fun process(stateAccess: StateAccess<State>) {
        supervisorScope { // TODO is this extra scope needed?
            actionFlow.collect { action ->
                launch { action.execute(stateAccess) }
            }
        }
    }

    override fun cancel(cause: Throwable?) {
        actionChannel.cancel(cause as? CancellationException)
    }
}
