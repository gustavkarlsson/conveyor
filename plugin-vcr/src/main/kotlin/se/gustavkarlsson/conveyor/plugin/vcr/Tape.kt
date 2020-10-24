package se.gustavkarlsson.conveyor.plugin.vcr

public interface Tape<T> : ReadableTape<T>, WriteableTape<T>, DeletableTape<T>
