package se.gustavkarlsson.conveyor

public interface Plugin<State> {
    public fun overrideInitialState( // TODO test
        initialState: State,
    ): State = initialState

    public fun overrideStartActions( // TODO test
        startActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = startActions
}
