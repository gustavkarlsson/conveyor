package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow
import se.gustavkarlsson.conveyor.Action

internal interface ActionFlowProvider<State> {
    val actionFlow: Flow<Action<State>>
}
