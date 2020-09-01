package se.gustavkarlsson.conveyor.test

import strikt.api.Assertion

class Counter {
    var value: Int = 0
        private set

    fun increment() {
        value++
    }
}

infix fun Assertion.Builder<Counter>.hasCount(expected: Int): Assertion.Builder<Counter> =
    assert("has count %s", expected) {
        when (val actual = it.value) {
            expected -> pass()
            else -> fail(actual = actual)
        }
    }
