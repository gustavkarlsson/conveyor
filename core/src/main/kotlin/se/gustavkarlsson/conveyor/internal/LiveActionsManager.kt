package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess

@ExperimentalCoroutinesApi
internal class LiveActionsManager<State>(
    actions: Iterable<Action<State>>,
    subscriptionCount: Flow<Int>,
) : ActionProcessor<State>, Cancellable {
    private val cancellationChannel = Channel<Unit>(Channel.CONFLATED)
        .apply { offerOrThrow(Unit) }

    private val toggleFlow = combine(
        subscriptionCount.distinctUntilChanged(),
        cancellationChannel.consumeAsFlow()
    ) { count, _ ->
        when {
            count < 0 -> error("count may not be negative")
            count == 0 -> Toggle.Disable
            else -> Toggle.Enable
        }
    }

    private var actions: Iterable<Action<State>>? = actions.toList()

    override suspend fun process(stateAccess: StateAccess<State>) {
        toggleFlow.collectLatest { toggle ->
            if (toggle == Toggle.Enable) {
                val actions = requireNotNull(actions)
                coroutineScope {
                    for (action in actions) {
                        launch { action.execute(stateAccess) }
                    }
                }
            }
        }
    }

    override fun cancel(cause: Throwable?) {
        cancellationChannel.cancel(cause as? CancellationException)
        actions = null
    }

    private enum class Toggle { Enable, Disable }
}
