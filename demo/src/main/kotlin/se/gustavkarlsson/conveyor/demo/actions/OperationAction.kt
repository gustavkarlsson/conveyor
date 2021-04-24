package se.gustavkarlsson.conveyor.demo.actions

import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow
import se.gustavkarlsson.conveyor.demo.Api
import se.gustavkarlsson.conveyor.demo.State

class OperationAction(private val api: Api) : Action<State> {
    override suspend fun execute(stateFlow: AtomicStateFlow<State>) {
        api.operation().collect { progress ->
            stateFlow.update {
                if (this is State.LoggedIn) {
                    copy(operationProgress = progress)
                } else this
            }
        }
        stateFlow.update {
            if (this is State.LoggedIn) {
                copy(operationProgress = null)
            } else this
        }
    }
}