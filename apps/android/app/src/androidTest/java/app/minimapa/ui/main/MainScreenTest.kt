package app.minimapa.ui.main

import app.minimapa.MainActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun portalOpensLoginAndDemoAccountOpensMap() {
    composeTestRule.onNodeWithText("TOQUE PARA ENTRAR").assertExists().performClick()
    composeTestRule.onNodeWithText("ENTRAR NO REINO").assertExists()
    composeTestRule.onNodeWithText("ENTRAR COM CONTA DE TESTE").performClick()

    composeTestRule.onNodeWithContentDescription("Mapa fantasia simulado de Rio Claro").assertExists()
    composeTestRule.onNodeWithText("MAPA SIMULADO  •  3 QUESTS").assertExists()
  }

  @Test
  fun questPinOpensQuestSheet() {
    openDemoMap()
    composeTestRule
      .onNodeWithContentDescription("Quest disponível: Entrega do Mercado Central")
      .performClick()

    composeTestRule.onNodeWithText("Entrega do Mercado Central").assertExists()
    composeTestRule.onNodeWithText("R$ 28").assertExists()
  }

  @Test
  fun actionMenuCreatesLocalDraft() {
    openDemoMap()
    composeTestRule.onNodeWithContentDescription("Abrir ações").performClick()
    composeTestRule.onNodeWithText("Postar uma quest").performClick()
    composeTestRule.onNodeWithText("SALVAR RASCUNHO LOCAL").performClick()

    composeTestRule.onNodeWithContentDescription("Rascunho local salvo").assertExists()
  }

  @Test
  fun accountCreationRejectsWeakPasswordBeforeNetwork() {
    composeTestRule.onNodeWithText("TOQUE PARA ENTRAR").performClick()
    composeTestRule.onNodeWithText("CRIAR UMA CONTA").performClick()
    composeTestRule.onNodeWithContentDescription("Campo de email").performTextInput("teste@minimapa.local")
    composeTestRule.onNodeWithContentDescription("Campo de senha").performTextInput("fraca")
    composeTestRule.onNodeWithText("CRIAR CONTA").performClick()

    composeTestRule.onNodeWithText("A senha precisa ter pelo menos 10 caracteres.").assertExists()
  }

  @Test
  fun recoveryUsesGenericAntiEnumerationMessage() {
    composeTestRule.onNodeWithText("TOQUE PARA ENTRAR").performClick()
    composeTestRule.onNodeWithText("ESQUECI MINHA SENHA").performClick()
    composeTestRule.onNodeWithContentDescription("Campo de email").performTextInput("alguem@minimapa.local")
    composeTestRule.onNodeWithText("ENVIAR RECUPERAÇÃO").performClick()

    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      composeTestRule
        .onAllNodesWithContentDescription("Mensagem de autenticação")
        .fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeTestRule.onNodeWithContentDescription("Mensagem de autenticação").assertExists()
  }

  private fun openDemoMap() {
    composeTestRule.onNodeWithText("TOQUE PARA ENTRAR").performClick()
    composeTestRule.onNodeWithText("ENTRAR COM CONTA DE TESTE").performClick()
  }
}
