package se.gustavkarlsson.conveyor

public fun interface Watcher<T> {
    public fun watch(value: T)
}
