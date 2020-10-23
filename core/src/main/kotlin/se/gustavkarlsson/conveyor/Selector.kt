package se.gustavkarlsson.conveyor

public interface Selector<T> {
    public suspend fun select(old: T, new: T): T
}
