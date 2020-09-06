package se.gustavkarlsson.conveyor.internal

internal interface WriteableStateContainer<State> {
    var currentState: State
}
