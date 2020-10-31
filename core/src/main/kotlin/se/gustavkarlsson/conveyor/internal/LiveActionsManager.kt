package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import se.gustavkarlsson.conveyor.Action
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
internal class LiveActionsManager<State>(
    actions: Iterable<Action<State>>,
) : LiveActionsCounter, ActionFlowProvider<State>, Cancellable {
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

    @FlowPreview
    override val actionFlow: Flow<Action<State>> = toggleChannel.consumeAsFlow()
        .distinctUntilChanged()
        .flatMapLatest { toggle ->
            when (toggle) {
                Toggle.Enable -> requireNotNull(this.actions).asFlow()
                Toggle.Disable -> emptyFlow()
            }
        }

    override fun cancel(cause: Throwable?) {
        toggleChannel.cancel(cause as? CancellationException)
        actions = null
    }

    private enum class Toggle { Enable, Disable }
}
