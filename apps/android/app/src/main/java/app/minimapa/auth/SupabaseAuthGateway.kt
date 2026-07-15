package app.minimapa.auth

import android.content.Intent
import app.minimapa.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient

class SupabaseAuthGateway private constructor(private val client: SupabaseClient?) : AuthGateway {
  override val isConfigured: Boolean = client != null

  override suspend fun signIn(email: String, password: String): AuthOutcome {
    val auth = client?.auth ?: return unavailable()
    return runCatching {
        auth.signInWith(Email) {
          this.email = email.trim()
          this.password = password
        }
        AuthOutcome.Authenticated(isNewAccount = false)
      }
      .getOrElse {
        AuthOutcome.Failure("Não foi possível entrar. Confira os dados ou tente novamente em instantes.")
      }
  }

  override suspend fun signUp(email: String, password: String): AuthOutcome {
    val auth = client?.auth ?: return unavailable()
    return runCatching {
        auth.signUpWith(Email, redirectUrl = AUTH_REDIRECT) {
          this.email = email.trim()
          this.password = password
        }
        val sessionCreated = auth.currentSessionOrNull() != null
        if (sessionCreated) {
          AuthOutcome.Authenticated(isNewAccount = true)
        } else {
          AuthOutcome.Notice(CONFIRMATION_MESSAGE)
        }
      }
      .getOrElse { AuthOutcome.Notice(CONFIRMATION_MESSAGE) }
  }

  override suspend fun requestPasswordReset(email: String): AuthOutcome {
    val auth = client?.auth ?: return unavailable()
    return runCatching {
        auth.resetPasswordForEmail(email.trim(), redirectUrl = AUTH_REDIRECT)
        AuthOutcome.Notice(RECOVERY_MESSAGE)
      }
      .getOrElse { AuthOutcome.Notice(RECOVERY_MESSAGE) }
  }

  fun handleDeepLink(intent: Intent) {
    client?.handleDeeplinks(intent)
  }

  private fun unavailable() =
    AuthOutcome.Failure("Supabase local indisponível. Use a conta de teste ou inicie o backend local.")

  companion object {
    private const val AUTH_REDIRECT = "minimapa://auth"
    private const val CONFIRMATION_MESSAGE =
      "Se os dados forem aceitos, a confirmação aparecerá no email local."
    private const val RECOVERY_MESSAGE =
      "Se a conta existir, as instruções de recuperação aparecerão no email local."

    fun fromBuildConfig(): SupabaseAuthGateway {
      val url = BuildConfig.SUPABASE_URL.trim()
      val key = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
      val client =
        if (url.isBlank() || key.isBlank()) {
          null
        } else {
          createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
            install(Auth) {
              scheme = "minimapa"
              host = "auth"
              flowType = FlowType.PKCE
            }
          }
        }
      return SupabaseAuthGateway(client)
    }
  }
}
