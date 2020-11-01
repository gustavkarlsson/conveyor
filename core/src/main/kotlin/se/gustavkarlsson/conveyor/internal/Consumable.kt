package se.gustavkarlsson.conveyor.internal

internal class Consumable<T: Any>(value: T) {
    private var innerValue: T? = value

    @Synchronized
    fun consume(): T {
        val value = checkNotNull(innerValue) { "value has already been consumed" }
        innerValue = null
        return value
    }
}
