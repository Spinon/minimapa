package app.minimapa.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.minimapa.auth.AuthGateway
import app.minimapa.auth.AuthOutcome
import app.minimapa.theme.ForestDeep
import app.minimapa.theme.ForestNight
import app.minimapa.theme.GuildGold
import app.minimapa.theme.Parchment
import app.minimapa.theme.ParchmentMuted
import kotlinx.coroutines.launch

private enum class AuthMode { LOGIN, SIGN_UP, RECOVERY }

@Composable
fun AuthScreen(
  gateway: AuthGateway,
  onAuthenticated: (isNewAccount: Boolean) -> Unit,
  onDemoAccount: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var mode by remember { mutableStateOf(AuthMode.LOGIN) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var busy by remember { mutableStateOf(false) }
  var notice by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()

  fun submit(block: suspend () -> AuthOutcome) {
    if (busy) return
    val validation = validate(mode, email, password)
    if (validation != null) {
      notice = validation
      return
    }
    scope.launch {
      busy = true
      notice = null
      when (val outcome = block()) {
        is AuthOutcome.Authenticated -> onAuthenticated(outcome.isNewAccount)
        is AuthOutcome.Notice -> notice = outcome.message
        is AuthOutcome.Failure -> notice = outcome.message
      }
      busy = false
    }
  }

  Column(
    modifier =
      modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(ForestNight, ForestDeep)))
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 18.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      OutlinedButton(onClick = onBack) { Text("VOLTAR") }
      Text(
        text = if (gateway.isConfigured) "SUPABASE LOCAL" else "MODO DEMO",
        color = GuildGold,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp),
      )
    }
    Spacer(Modifier.height(28.dp))
    Text("MINIMAPA", color = Parchment, fontWeight = FontWeight.Black, fontSize = 34.sp)
    Text(
      text =
        when (mode) {
          AuthMode.LOGIN -> "Identifique seu aventureiro"
          AuthMode.SIGN_UP -> "Registre-se no reino"
          AuthMode.RECOVERY -> "Recupere sua chave de acesso"
        },
      color = ParchmentMuted,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(28.dp))
    OutlinedTextField(
      value = email,
      onValueChange = { email = it; notice = null },
      label = { Text("Email") },
      singleLine = true,
      enabled = !busy,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
      modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Campo de email" },
    )
    if (mode != AuthMode.RECOVERY) {
      Spacer(Modifier.height(12.dp))
      OutlinedTextField(
        value = password,
        onValueChange = { password = it; notice = null },
        label = { Text("Senha") },
        supportingText =
          if (mode == AuthMode.SIGN_UP) {
            { Text("10+ caracteres, maiúscula, minúscula e número") }
          } else {
            null
          },
        singleLine = true,
        enabled = !busy,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Campo de senha" },
      )
    }
    notice?.let {
      Spacer(Modifier.height(14.dp))
      Text(
        text = it,
        color = Parchment,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { contentDescription = "Mensagem de autenticação" },
      )
    }
    Spacer(Modifier.height(22.dp))
    Button(
      onClick = {
        when (mode) {
          AuthMode.LOGIN -> submit { gateway.signIn(email, password) }
          AuthMode.SIGN_UP -> submit { gateway.signUp(email, password) }
          AuthMode.RECOVERY -> submit { gateway.requestPasswordReset(email) }
        }
      },
      enabled = !busy,
      colors = ButtonDefaults.buttonColors(containerColor = GuildGold, contentColor = ForestNight),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier.fillMaxWidth().height(54.dp),
    ) {
      if (busy) {
        CircularProgressIndicator(color = ForestNight)
      } else {
        Text(
          text =
            when (mode) {
              AuthMode.LOGIN -> "ENTRAR NO REINO"
              AuthMode.SIGN_UP -> "CRIAR CONTA"
              AuthMode.RECOVERY -> "ENVIAR RECUPERAÇÃO"
            },
          fontWeight = FontWeight.Black,
        )
      }
    }
    Spacer(Modifier.height(10.dp))
    when (mode) {
      AuthMode.LOGIN -> {
        OutlinedButton(onClick = { mode = AuthMode.RECOVERY; notice = null }, modifier = Modifier.fillMaxWidth()) {
          Text("ESQUECI MINHA SENHA")
        }
        OutlinedButton(onClick = { mode = AuthMode.SIGN_UP; notice = null }, modifier = Modifier.fillMaxWidth()) {
          Text("CRIAR UMA CONTA")
        }
      }
      AuthMode.SIGN_UP, AuthMode.RECOVERY ->
        OutlinedButton(onClick = { mode = AuthMode.LOGIN; notice = null }, modifier = Modifier.fillMaxWidth()) {
          Text("VOLTAR AO LOGIN")
        }
    }
    Spacer(Modifier.height(18.dp))
    OutlinedButton(onClick = onDemoAccount, modifier = Modifier.fillMaxWidth()) {
      Text("ENTRAR COM CONTA DE TESTE")
    }
    Text(
      "A conta de teste não representa autenticação real.",
      color = ParchmentMuted,
      style = MaterialTheme.typography.bodySmall,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(18.dp))
    OutlinedButton(onClick = { notice = "Google está em mock até a configuração segura das credenciais." }, modifier = Modifier.fillMaxWidth()) {
      Text("CONTINUAR COM GOOGLE · MOCK")
    }
  }
}

private fun validate(mode: AuthMode, email: String, password: String): String? {
  if (!email.contains('@') || email.substringAfter('@').isBlank()) return "Informe um email válido."
  if (mode == AuthMode.RECOVERY) return null
  if (password.length < 10) return "A senha precisa ter pelo menos 10 caracteres."
  if (mode == AuthMode.SIGN_UP &&
    (!password.any(Char::isUpperCase) || !password.any(Char::isLowerCase) || !password.any(Char::isDigit))
  ) {
    return "Use ao menos uma letra maiúscula, uma minúscula e um número."
  }
  return null
}
