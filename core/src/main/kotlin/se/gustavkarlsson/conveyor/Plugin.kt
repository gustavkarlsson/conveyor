package se.gustavkarlsson.conveyor

public interface Plugin<State> {
    public fun overrideInitialState( // TODO test
        initialState: State,
    ): State = initialState

    public fun overrideStartActions( // TODO test
        startActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = startActions

    public fun overrideActionTransformers( // TODO test
        actionTransformers: Iterable<Transformer<Action<State>>>,
    ): Iterable<Transformer<Action<State>>> = actionTransformers

    public fun overrideStateTransformers( // TODO test
        stateTransformers: Iterable<Transformer<State>>,
    ): Iterable<Transformer<State>> = stateTransformers
}
