package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class StateTransformer<State>(
    private val incomingState: StateFlow<State>,
    private val transformers: Iterable<Transformer<State>>,
) : Launcher {
    private val mutableState = MutableStateFlow(incomingState.value)
    val outgoingState: StateFlow<State> = mutableState

    // FIXME test
    val outgoingSubscriberCount: StateFlow<Int> by mutableState::subscriptionCount
    override fun launch(scope: CoroutineScope): Job = scope.launch {
        incomingState
            .transform(transformers)
            .collect { mutableState.value = it }
    }
}
