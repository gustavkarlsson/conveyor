package se.gustavkarlsson.conveyor.demo

private val EMAIL_REGEX = Regex(".+@.+")

private const val PASSWORD_MIN_LENGTH = 6

sealed class State {
    data class Login(
        val emailText: String = "",
        val passwordText: String = "",
        val isLoggingIn: Boolean = false,
        val showInvalidLogin: Boolean = false,
    ) : State() {
        val isLoginIndicatorVisible: Boolean get() = isLoggingIn
        val isLoginButtonEnabled: Boolean
            get() = !isLoggingIn &&
                emailText.matches(EMAIL_REGEX) &&
                passwordText.length >= PASSWORD_MIN_LENGTH
    }

    data class LoggedIn(
        val name: String,
        val operationProgress: Float? = null,
    ) : State() {
        private val isOperating: Boolean get() = operationProgress != null
        val isLogoutButtonEnabled: Boolean get() = !isOperating
        val isOperationButtonEnabled: Boolean get() = !isOperating
        val isOperationIndicatorVisible: Boolean get() = isOperating
        val operationIndicatorProgress: Float get() = operationProgress ?: 0F
    }
}
