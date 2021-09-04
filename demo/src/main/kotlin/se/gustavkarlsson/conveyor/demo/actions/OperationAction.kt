package se.gustavkarlsson.conveyor.demo.actions

import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.demo.Api
import se.gustavkarlsson.conveyor.demo.State

class OperationAction(private val api: Api) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        api.operation().collect { progress ->
            storeFlow.update { state ->
                if (state is State.LoggedIn) {
                    state.copy(operationProgress = progress)
                } else state
            }
        }
        storeFlow.update { state ->
            if (state is State.LoggedIn) {
                state.copy(operationProgress = null)
            } else state
        }
    }
}
