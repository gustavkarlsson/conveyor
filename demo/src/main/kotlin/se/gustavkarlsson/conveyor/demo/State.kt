package se.gustavkarlsson.conveyor.demo

private val EMAIL_REGEX = Regex(".+@.+")

private const val PASSWORD_MIN_LENGTH = 6

sealed class State {
    data class Login(
        val emailText: String = "",
        val passwordText: String = "",
        val loginStage: LoginStage = LoginStage.Initial,
    ) : State() {
        val isLoginIndicatorVisible: Boolean
            get() = loginStage is LoginStage.LoggingIn
        val loginIndicatorProgress: Float
            get() = (loginStage as? LoginStage.LoggingIn)?.progress ?: 0F
        val isLoginButtonEnabled: Boolean
            get() = loginStage == LoginStage.Initial &&
                emailText.matches(EMAIL_REGEX) &&
                passwordText.length >= PASSWORD_MIN_LENGTH
    }

    data class LoggedIn(
        val emailText: String,
    ) : State()
}

sealed class LoginStage {
    object Initial : LoginStage()
    data class LoggingIn(val progress: Float) : LoginStage()
}
