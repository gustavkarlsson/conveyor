package se.gustavkarlsson.conveyor.plugin.vcr.internal

internal interface Tape<State> : ReadableTape<State>, WriteableTape<State>
