package se.gustavkarlsson.conveyor

/**
 * Enables changing the behavior of a [Store] by modifying how it's created and how it processes actions and the state.
 */
public interface Plugin<State> {
    /**
     * Override the initial state of the [Store].
     */
    public fun overrideInitialState(
        initialState: State,
    ): State = initialState

    /**
     * Override the start actions of the [Store].
     */
    public fun overrideStartActions(
        startActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = startActions

    /**
     * Override the action transformers of the [Store].
     * Action transformers change the flow of actions before they are processed.
     */
    public fun overrideActionTransformers(
        actionTransformers: Iterable<Transformer<Action<State>>>,
    ): Iterable<Transformer<Action<State>>> = actionTransformers

    /**
     * Override the state transformers of the [Store].
     * State transformers change the "external" state exposed by [Store.state].
     *
     * *Note: State transformers do NOT change the "internal" state that actions operate on.*
     */
    public fun overrideStateTransformers(
        stateTransformers: Iterable<Transformer<State>>,
    ): Iterable<Transformer<State>> = stateTransformers
}
