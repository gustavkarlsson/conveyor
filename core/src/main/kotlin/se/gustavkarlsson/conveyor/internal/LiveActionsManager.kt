package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
internal class LiveActionsManager<State>(
    actions: Iterable<Action<State>>,
) : LiveActionsCounter, ActionProcessor<State>, Cancellable {
    private val liveCount = AtomicInteger(0)
    private val toggleChannel = Channel<Toggle>(Channel.CONFLATED)
    private val toggleFlow = toggleChannel.consumeAsFlow()
        .distinctUntilChanged()

    override fun increment() {
        if (liveCount.incrementAndGet() == 1) {
            toggleChannel.offerOrThrow(Toggle.Enable)
        }
    }

    override fun decrement() {
        val newCount = liveCount.decrementAndGet()
        check(newCount >= 0)
        if (newCount == 0) {
            toggleChannel.offerOrThrow(Toggle.Disable)
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
        toggleChannel.cancel(cause as? CancellationException)
        actions = null
    }

    private enum class Toggle { Enable, Disable }
}
