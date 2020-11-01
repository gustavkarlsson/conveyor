package se.gustavkarlsson.conveyor.internal

// TODO Add tests
internal class Consumable<T>(value: T) {
    private var consumed = false
    private var innerValue: T? = value

    @Synchronized
    fun consume(): T {
        check(!consumed) { "value has already been consumed" }
        val value = innerValue
        innerValue = null
        consumed = true
        return value!!
    }
}
