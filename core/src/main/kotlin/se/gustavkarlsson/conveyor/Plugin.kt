package se.gustavkarlsson.conveyor

public interface Plugin<State> {
    public fun overrideInitialState(
        initialState: State,
    ): State = initialState

    public fun overrideStartActions(
        startActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = startActions

    public fun overrideLiveActions(
        liveActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = liveActions

    public fun overrideStateSelectors(
        stateSelectors: Iterable<Selector<State>>,
    ): Iterable<Selector<State>> = stateSelectors

    public fun overrideActionTransformers(
        actionTransformers: Iterable<Transformer<Action<State>>>,
    ): Iterable<Transformer<Action<State>>> = actionTransformers
}
