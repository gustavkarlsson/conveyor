package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer
import java.util.concurrent.atomic.AtomicInteger

// TODO more testing required
@ExperimentalCoroutinesApi
internal class LiveActionsManager<State>(
    actions: Iterable<Action<State>>,
    private val commandIssuer: CommandIssuer<State>,
) : LiveActionsCounter, Processor, Cancellable {
    private val toggleChannel = Channel<Toggle>(Channel.CONFLATED)

    private var actions: Iterable<Action<State>>? = actions.toList()

    private val flow = toggleChannel.consumeAsFlow()
        .distinctUntilChanged()
        .mapLatest { toggle ->
            when (toggle) {
                Toggle.Enable -> requireNotNull(this.actions)
                Toggle.Disable -> emptyList()
            }
        }

    private val liveCount = AtomicInteger(0)

    override suspend fun increaseLiveCount() {
        if (liveCount.incrementAndGet() == 1) {
            toggleChannel.send(Toggle.Enable)
        }
    }

    override suspend fun decreaseLiveCount() {
        if (liveCount.decrementAndGet() == 0) {
            toggleChannel.send(Toggle.Disable)
        }
    }

    override suspend fun process(scope: CoroutineScope) =
        flow.collectLatest { actions ->
            for (action in actions) {
                scope.launch { action.execute(commandIssuer) }
            }
        }

    override fun cancel(cause: Throwable?) {
        toggleChannel.cancel(cause as? CancellationException)
        actions = null
    }

    private enum class Toggle { Enable, Disable }
}
