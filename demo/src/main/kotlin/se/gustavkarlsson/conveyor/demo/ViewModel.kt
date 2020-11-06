@file:Suppress("EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ViewModel {
    val state: StateFlow<ViewState>
    fun onInputTextChanged(text: String)
    fun onInputTextSubmitted()
    fun onDecrementButtonClicked(name: String)
    fun onIncrementButtonClicked(name: String)
}

private val sampleState = State(
    items = listOf(
        Item("Milk", 2),
        Item("Beer", 6),
    ),
)

fun createViewModel(): ViewModel = ViewModelImpl(sampleState)

private data class State(
    val inputText: String = "",
    val items: List<Item> = emptyList(),
)

private class ViewModelImpl(initialState: State = State()) : ViewModel {
    private var currentState = initialState

    private val mutableState = MutableStateFlow(currentState.toViewState())
    override val state: StateFlow<ViewState> = mutableState

    override fun onInputTextChanged(text: String) = updateState {
        copy(inputText = text)
    }

    override fun onInputTextSubmitted() = updateState {
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

    override fun onDecrementButtonClicked(name: String) = updateState {
        val newItems = items
            .updateCountOf(name) { it - 1 }
            .filter { it.count > 0 }
        copy(items = newItems)
    }

    override fun onIncrementButtonClicked(name: String) = updateState {
        val newItems = items
            .updateCountOf(name) { it + 1 }
        copy(items = newItems)
    }

    @Synchronized
    private fun updateState(block: State.() -> State) {
        mutableState.value = currentState.block().toViewState()
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

    private fun State.toViewState(): ViewState = ViewState(
        inputText = inputText,
        addButtonEnabled = inputText.isNotBlank(),
        items = items,
    )
}
