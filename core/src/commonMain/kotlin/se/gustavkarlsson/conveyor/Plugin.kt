package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow

/**
 * Enables changing the behavior of a [Store] by modifying how it's created and how it processes actions and the state.
 */
public interface Plugin<State> {
    /**
     * Override the initial state of the [Store].
     */
    public fun overrideInitialState(initialState: State): State = initialState

    /**
     * Add additional start actions to the [Store].
     */
    public fun addStartActions(): Iterable<Action<State>> = emptyList()

    /**
     * Transform the flow of actions before they are executed.
     */
    public fun transformActions(actions: Flow<Action<State>>): Flow<Action<State>> = actions

    /**
     * Transform the flow of "external" states exposed by [Store.state].
     *
     * *Note: Does NOT change the "internal" state that actions operate on.*
     */
    public fun transformStates(states: Flow<State>): Flow<State> = states
}
