package se.gustavkarlsson.conveyor

public interface Plugin<State> {
    public fun overrideInitialState(
        initialState: State,
    ): State = initialState

    public fun overrideOpenActions(
        openActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = openActions

    public fun overrideLiveActions(
        liveActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = liveActions

    public fun overrideStateMappers(
        stateMappers: Iterable<Mapper<State>>,
    ): Iterable<Mapper<State>> = stateMappers
}
