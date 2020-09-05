package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

private const val DEFAULT_BUFFER_SIZE = 64

// TODO Add support for cold sources? https://www.halfbit.de/posts/external-events-in-knot/
// TODO Add support for watchers?
// TODO Add support for side effects/events/interceptors
// TODO React to state/changes?

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    initialState: State,
    initialActions: Iterable<Action<State>> = emptyList(),
    private val onlineActions: Iterable<Action<State>> = emptyList(),
    commandBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : Store<State> {

    private val stateHolder = StateHolder(initialState)

    private var onlineScope: CoroutineScope? = null
    private var onlineCount = 0

    override val state = stateHolder.flow
        .onStart {
            val newScope = synchronized(this@StoreImpl) {
                if (++onlineCount == 1) {
                    // Just came online
                    check(onlineScope == null)
                    onlineScope = CoroutineScope(Job())
                    onlineScope
                } else {
                    null
                }
            }
            newScope?.launch {
                for (action in onlineActions) {
                    launch { action.execute(commandProcessor) }
                }
            }
        }
        .onCompletion { cause ->
            val scopeToCancel = synchronized(this@StoreImpl) {
                if (--onlineCount == 0) {
                    // Just came offline
                    val existingScope = checkNotNull(onlineScope)
                    onlineScope = null
                    existingScope
                } else {
                    null
                }
            }
            scopeToCancel?.cancel(cause as? CancellationException)
        }

    override val currentState get() = stateHolder.get()

    private val commandProcessor = CommandProcessor(commandBufferSize, stateHolder::get, stateHolder::set)

    private val initialActions = AtomicReference(initialActions)

    override fun start(scope: CoroutineScope): Job {
        val actions = takeInitialActions()
        val job = scope.launch {
            actions.removeAll { action ->
                launch { action.execute(commandProcessor) }
                true
            }
            commandProcessor.process { action ->
                launch { action.execute(commandProcessor) }
            }
        }
        job.invokeOnCompletion { throwable ->
            commandProcessor.close(throwable)
            stateHolder.close(throwable)
        }
        return job
    }

    private fun takeInitialActions(): MutableIterable<Action<State>> {
        val actions = checkNotNull(initialActions.getAndSet(null)) {
            "Store has already been started"
        }
        return ArrayDeque(actions.toList())
    }

    override suspend fun issue(command: Command<State>) = commandProcessor.issue(command)
}

@FlowPreview
@ExperimentalCoroutinesApi
private class StateHolder<State>(initialState: State) {
    private val channel = ConflatedBroadcastChannel(initialState)

    fun get(): State = channel.value

    fun set(state: State) {
        check(channel.offer(state)) {
            "Failed to set state, channel over capacity"
        }
    }

    val flow: Flow<State> =
        channel.asFlow()
            .distinctUntilChanged { old, new -> old === new }

    fun close(cause: Throwable?) {
        channel.close(cause)
    }
}

@ExperimentalCoroutinesApi
private class CommandProcessor<State>(
    bufferSize: Int,
    private val getState: () -> State,
    private val setState: (State) -> Unit,
) : CommandIssuer<State> {
    init {
        require(bufferSize > 0) {
            "bufferSize must be positive. Was: $bufferSize"
        }
    }

    private val channel = Channel<Command<State>>(bufferSize)

    override suspend fun issue(command: Command<State>) {
        check(!channel.isClosedForSend) { "Store has been stopped" }
        channel.send(command)
    }

    suspend fun process(onAction: suspend (Action<State>) -> Unit) =
        channel.consumeEach { command ->
            val oldState = getState()
            val (newState, actions) = command.reduce(oldState)
            setState(newState)
            for (action in actions) {
                onAction(action)
            }
        }

    fun close(cause: Throwable?) {
        channel.close(cause)
    }
}
