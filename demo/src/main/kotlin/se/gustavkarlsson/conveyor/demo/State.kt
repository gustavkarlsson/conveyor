package se.gustavkarlsson.conveyor.demo

private val EMAIL_REGEX = Regex(".+@.+")

private const val PASSWORD_MIN_LENGTH = 6

sealed class State {
    data class Login(
        val emailText: String = "",
        val passwordText: String = "",
        val loginProgress: Float? = null,
    ) : State() {
        val isLoggingIn: Boolean get() = loginProgress != null
        val isLoginIndicatorVisible: Boolean get() = isLoggingIn
        val loginIndicatorProgress: Float get() = loginProgress ?: 0F
        val isLoginButtonEnabled: Boolean
            get() = !isLoggingIn &&
                emailText.matches(EMAIL_REGEX) &&
                passwordText.length >= PASSWORD_MIN_LENGTH
    }

    data class LoggedIn(
        val emailText: String,
    ) : State()
}
