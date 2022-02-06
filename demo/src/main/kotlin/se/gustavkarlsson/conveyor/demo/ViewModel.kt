package se.gustavkarlsson.conveyor.demo

import kotlinx.coroutines.flow.StateFlow
import se.gustavkarlsson.conveyor.Store
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

class ViewModel(api: Api) : LoginEvents, LoggedInEvents {
    private val store = Store<State>(initialState = State.Login())
    val state: StateFlow<State> = store.state
    private val loginAction = LoginAction(api)
    private val operationAction = OperationAction(api)

    suspend fun run(): Nothing = store.run()

    override fun onEmailTextChanged(text: String) = store.issue(ChangeEmailAction(text))

    override fun onPasswordTextChanged(text: String) = store.issue(ChangePasswordAction(text))

    override fun onLoginButtonClicked() = store.issue(loginAction)

    override fun onOperationButtonClicked() = store.issue(operationAction)

    override fun onLogoutButtonClicked() = store.issue { state ->
        state.update { State.Login() }
    }
}
