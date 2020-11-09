@file:Suppress("EXPERIMENTAL_API_USAGE")

package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.buildStore
import se.gustavkarlsson.conveyor.demo.actions.ChangeEmailAction
import se.gustavkarlsson.conveyor.demo.actions.ChangePasswordAction
import se.gustavkarlsson.conveyor.demo.actions.LoginAction
import se.gustavkarlsson.conveyor.demo.actions.OperationAction
import se.gustavkarlsson.conveyor.issue

interface LoginEvents {
    fun onEmailTextChanged(text: String)
    fun onPasswordTextChanged(text: String)
    fun onLoginButtonClicked()
}

interface LoggedInEvents {
    fun onOperationButtonClicked()
    fun onLogoutButtonClicked()
}

class ViewModel(api: Api, initialState: State) : LoginEvents, LoggedInEvents {
    private val store = buildStore(initialState).apply { start(GlobalScope) }
    val state: StateFlow<State> = store.state
    private val loginAction = LoginAction(api)
    private val operationAction = OperationAction(api)

    override fun onEmailTextChanged(text: String) = store.issue(ChangeEmailAction(text))

    override fun onPasswordTextChanged(text: String) = store.issue(ChangePasswordAction(text))

    override fun onLoginButtonClicked() = store.issue(loginAction)

    override fun onOperationButtonClicked() = store.issue(operationAction)

    override fun onLogoutButtonClicked() = store.issue { state ->
        state.update { State.Login() }
    }
}
