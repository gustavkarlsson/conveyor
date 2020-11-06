@file:Suppress("EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ViewModel(initialState: ViewState) {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<ViewState> = mutableState

    fun onInputTextChanged(text: String) = updateState {
        copy(inputText = text)
    }

    fun onInputTextSubmitted() = updateState {
        var found = false
        val newItems = items
            .updateCountOf(inputText) {
                found = true
                it + 1
            }
            .takeIf { found }
            ?: listOf(Item(inputText)) + items
        copy(inputText = "", items = newItems)
    }

    fun onDecrementButtonClicked(name: String) = updateState {
        val newItems = items
            .updateCountOf(name) { it - 1 }
            .filter { it.count > 0 }
        copy(items = newItems)
    }

    fun onIncrementButtonClicked(name: String) = updateState {
        val newItems = items
            .updateCountOf(name) { it + 1 }
        copy(items = newItems)
    }

    @Synchronized
    private fun updateState(block: ViewState.() -> ViewState) {
        mutableState.value = mutableState.value.block()
    }

    private inline fun List<Item>.updateCountOf(
        name: String,
        operation: (Int) -> Int,
    ): List<Item> = map {
        if (it.name.normalize() == name.normalize()) {
            it.copy(count = operation(it.count))
        } else it
    }

    private fun String.normalize(): String = trim().toLowerCase()
}
