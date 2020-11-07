package se.gustavkarlsson.conveyor.demo

private val EMAIL_REGEX = Regex(".+@.+")

private const val PASSWORD_MIN_LENGTH = 6

sealed class ViewState {
    data class Login(
        val emailText: String = "",
        val passwordText: String = "",
        val loginState: LoginState = LoginState.Initial,
    ) : ViewState() {
        val isLoginIndicatorVisible: Boolean
            get() = loginState is LoginState.LoggingIn
        val loginIndicatorProgress: Float
            get() = (loginState as? LoginState.LoggingIn)?.progress ?: 0F
        val isLoginButtonEnabled: Boolean
            get() = loginState == LoginState.Initial &&
                emailText.matches(EMAIL_REGEX) &&
                passwordText.length > PASSWORD_MIN_LENGTH
    }

    data class LoggedIn(
        val emailText: String,
    ) : ViewState()
}

sealed class LoginState {
    object Initial : LoginState()
    data class LoggingIn(val progress: Float) : LoginState()
}
