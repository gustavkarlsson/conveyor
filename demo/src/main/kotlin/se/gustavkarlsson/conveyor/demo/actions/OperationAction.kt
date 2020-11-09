package se.gustavkarlsson.conveyor.demo.actions

import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.demo.Api
import se.gustavkarlsson.conveyor.demo.State

class OperationAction(private val api: Api) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        api.operation().collect { progress ->
            state.update {
                if (this is State.LoggedIn) {
                    copy(operationProgress = progress)
                } else this
            }
        }
        state.update {
            if (this is State.LoggedIn) {
                copy(operationProgress = null)
            } else this
        }
    }
}
