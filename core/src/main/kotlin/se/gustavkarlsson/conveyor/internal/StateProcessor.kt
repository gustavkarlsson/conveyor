package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Transformer

// TODO Test
internal class StateProcessor<State>(
    private val incomingState: UpdatableStateFlowImpl<State>,
    private val transformers: Iterable<Transformer<State>>,
) : Processor {
    private val mutableState = MutableStateFlow(incomingState.value)
    val outgoingState: StateFlow<State> = mutableState
    override fun process(scope: CoroutineScope): Job = scope.launch {
        incomingState
            .transform(transformers)
            .collect { mutableState.value = it }
    }
}
