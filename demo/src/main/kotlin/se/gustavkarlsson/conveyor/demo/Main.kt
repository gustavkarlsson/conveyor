@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

fun main() {
    val sampleState = ViewState(
        items = listOf(
            Item("Milk", 2),
            Item("Beer", 6),
        ),
    )
    runUi(ViewModel(sampleState))
}

private fun runUi(viewModel: ViewModel) = Window(
    title = "Compose for Desktop",
    size = IntSize(400, 400),
) {
    val state = viewModel.state.collectAsState(viewModel.state.value)
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AddItemRow(state, viewModel)
            ItemsList(state, viewModel)
        }
    }
}

@Composable
private fun AddItemRow(
    state: State<ViewState>,
    viewModel: ViewModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextField(
            value = state.value.inputText,
            onValueChange = viewModel::onInputTextChanged,
            placeholder = { Text("Item") },
            maxLines = 1,
        )
        Button(onClick = viewModel::onInputTextSubmitted, enabled = state.value.addButtonEnabled) {
            Text("Add")
        }
    }
}

@Composable
private fun ItemsList(state: State<ViewState>, viewModel: ViewModel) {
    LazyColumnForIndexed(
        modifier = Modifier.fillMaxSize(),
        items = state.value.items,
    ) { index, item ->
        ItemRow(item, index, viewModel)
    }
}

@Composable
private fun ItemRow(
    item: Item,
    index: Int,
    viewModel: ViewModel,
) {
    val alternateRowColor = MaterialTheme.colors.onSurface.copy(alpha = 0.05f)
    val rowModifier = Modifier.fillMaxWidth()
        .let { modifier ->
            if (index % 2 == 1) {
                modifier.background(alternateRowColor)
            } else modifier
        }
        .padding(8.dp)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(modifier = Modifier.padding(end = 8.dp), text = item.name)
        CountSection(viewModel, item)
    }
}

@Composable
private fun CountSection(
    viewModel: ViewModel,
    item: Item,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier
                .padding(4.dp)
                .clickable { viewModel.onDecrementButtonClicked(item.name) }
                .padding(4.dp),
            text = "-",
        )
        Text(text = item.count.toString())
        Text(
            modifier = Modifier
                .padding(4.dp)
                .clickable { viewModel.onIncrementButtonClicked(item.name) }
                .padding(4.dp),
            text = "+",
        )
    }
}
