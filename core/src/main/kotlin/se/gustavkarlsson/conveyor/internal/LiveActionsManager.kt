package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import se.gustavkarlsson.conveyor.Action
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
internal class LiveActionsManager<State>(
    actions: Iterable<Action<State>>,
) : LiveActionsCounter, ActionProcessor<State>, Cancellable {
    private val liveCount = AtomicInteger(0)
    private val toggleChannel = Channel<Toggle>(Channel.CONFLATED)

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

    private val flow = toggleChannel.consumeAsFlow()
        .distinctUntilChanged()
        .mapLatest { toggle ->
            when (toggle) {
                Toggle.Enable -> requireNotNull(this.actions)
                Toggle.Disable -> emptyList()
            }
        }

    override suspend fun process(onAction: suspend (Action<State>) -> Unit) =
        flow.collectLatest { actions ->
            for (action in actions) {
                onAction(action)
            }
        }

    override fun cancel(cause: Throwable?) {
        toggleChannel.cancel(cause as? CancellationException)
        actions = null
    }

    private enum class Toggle { Enable, Disable }
}
