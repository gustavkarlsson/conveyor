package se.gustavkarlsson.conveyor.demo

data class ViewState(
    val inputText: String = "",
    val items: List<Item> = emptyList(),
) {
    val addButtonEnabled: Boolean
        get() = inputText.isNotBlank()
}

data class Item(val name: String, val count: Int = 1)
