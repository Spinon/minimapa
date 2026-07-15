package app.minimapa.ui.main

import app.minimapa.MainActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun portalOpensFantasyMap() {
    composeTestRule.onNodeWithText("TOQUE PARA ENTRAR").assertExists().performClick()

    composeTestRule.onNodeWithContentDescription("Mapa fantasia simulado de Rio Claro").assertExists()
    composeTestRule.onNodeWithText("MAPA SIMULADO  •  3 QUESTS").assertExists()
  }

  @Test
  fun questPinOpensQuestSheet() {
    composeTestRule.onNodeWithText("TOQUE PARA ENTRAR").performClick()
    composeTestRule
      .onNodeWithContentDescription("Quest disponível: Entrega do Mercado Central")
      .performClick()

    composeTestRule.onNodeWithText("Entrega do Mercado Central").assertExists()
    composeTestRule.onNodeWithText("R$ 28").assertExists()
  }

  @Test
  fun actionMenuCreatesLocalDraft() {
    composeTestRule.onNodeWithText("TOQUE PARA ENTRAR").performClick()
    composeTestRule.onNodeWithContentDescription("Abrir ações").performClick()
    composeTestRule.onNodeWithText("Postar uma quest").performClick()
    composeTestRule.onNodeWithText("SALVAR RASCUNHO LOCAL").performClick()

    composeTestRule.onNodeWithContentDescription("Rascunho local salvo").assertExists()
  }
}
