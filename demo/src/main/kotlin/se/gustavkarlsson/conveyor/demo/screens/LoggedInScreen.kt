@file:Suppress("FunctionName")

package se.gustavkarlsson.conveyor.demo.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import se.gustavkarlsson.conveyor.demo.LoggedInEvents
import se.gustavkarlsson.conveyor.demo.State

@Composable
fun LoggedInScreen(state: State.LoggedIn, events: LoggedInEvents) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        LogoutButton(state.isLogoutButtonEnabled, events::onLogoutButtonClicked)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LoggedInTitle()
            LoggedInNameText(state.name)
            OperationButton(state.isOperationButtonEnabled, events::onOperationButtonClicked)
            OperationIndicator(state.isOperationIndicatorVisible, state.operationIndicatorProgress)
        }
    }
}

@Composable
private fun LogoutButton(enabled: Boolean, onClick: () -> Unit) {
    TextButton(
        modifier = Modifier.padding(8.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Text("LOGOUT")
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
private fun LoggedInNameText(text: String) {
    Text(
        modifier = Modifier.padding(8.dp),
        text = text,
        style = MaterialTheme.typography.h6,
    )
}

@Composable
private fun OperationButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier.padding(8.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Text("EXPENSIVE OPERATION")
    }
}

@Composable
private fun OperationIndicator(isVisible: Boolean, progress: Float) {
    val opacity = if (isVisible) 1F else 0F
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )
    LinearProgressIndicator(
        modifier = Modifier.padding(4.dp).alpha(opacity),
        color = MaterialTheme.colors.secondary,
        progress = animatedProgress,
    )
}
