package se.gustavkarlsson.conveyor.demo

private val EMAIL_REGEX = Regex(".+@.+")

private const val PASSWORD_MIN_LENGTH = 6

data class ViewState(
    val emailText: String = "",
    val passwordText: String = "",
    val loginState: LoginState = LoginState.Initial,
) {
    val loginIndicatorProgress: Float?
        get() = (loginState as? LoginState.LoggingIn)?.progress
    val isLoginButtonEnabled: Boolean
        get() = loginState == LoginState.Initial &&
            emailText.matches(EMAIL_REGEX) &&
            passwordText.length > PASSWORD_MIN_LENGTH
}

sealed class LoginState {
    object Initial : LoginState()
    data class LoggingIn(val progress: Float) : LoginState()
    object LoggedIn : LoginState()
}
