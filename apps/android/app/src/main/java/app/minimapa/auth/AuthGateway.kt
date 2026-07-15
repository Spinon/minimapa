package app.minimapa.auth

sealed interface AuthOutcome {
  data class Authenticated(val isNewAccount: Boolean) : AuthOutcome

  data class Notice(val message: String) : AuthOutcome

  data class Failure(val message: String) : AuthOutcome
}

interface AuthGateway {
  val isConfigured: Boolean

  suspend fun signIn(email: String, password: String): AuthOutcome

  suspend fun signUp(email: String, password: String): AuthOutcome

  suspend fun requestPasswordReset(email: String): AuthOutcome
}

object DemoAuthGateway : AuthGateway {
  override val isConfigured = false

  override suspend fun signIn(email: String, password: String): AuthOutcome =
    AuthOutcome.Authenticated(isNewAccount = false)

  override suspend fun signUp(email: String, password: String): AuthOutcome =
    AuthOutcome.Authenticated(isNewAccount = true)

  override suspend fun requestPasswordReset(email: String): AuthOutcome =
    AuthOutcome.Notice("Se a conta existir, as instruções aparecerão no email local.")
}
