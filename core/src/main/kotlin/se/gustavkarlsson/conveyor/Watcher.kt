package se.gustavkarlsson.conveyor

public interface Watcher<T> {
    public suspend fun watch(value: T)
}
