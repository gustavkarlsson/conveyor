package se.gustavkarlsson.conveyor.demo

data class ViewState(
    val inputText: String,
    val addButtonEnabled: Boolean,
    val items: List<Item>,
)

data class Item(val name: String, val count: Int = 1)
