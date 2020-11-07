@file:Suppress("FunctionName")

package se.gustavkarlsson.conveyor.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoggedInScreen(state: State<ViewState.LoggedIn>, viewModel: ViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LoggedInTitle()
        LoggedInEmailText(state)
        LogoutButton(viewModel)
    }
}

@Composable
private fun LoggedInTitle() {
    Text(
        modifier = Modifier.padding(8.dp),
        text = "Welcome",
        style = MaterialTheme.typography.h4.copy(color = MaterialTheme.colors.secondary),
    )
}

@Composable
private fun LoggedInEmailText(state: State<ViewState.LoggedIn>) {
    Text(
        modifier = Modifier.padding(8.dp),
        text = state.value.emailText,
        style = MaterialTheme.typography.h5,
    )
}

@Composable
private fun LogoutButton(viewModel: ViewModel) {
    Button(
        modifier = Modifier.padding(8.dp),
        onClick = viewModel::onLogoutButtonClicked,
    ) {
        Text("LOGOUT")
    }
}
