package se.gustavkarlsson.conveyor.store

internal interface WriteableStateContainer<State> {
    var currentState: State
}
