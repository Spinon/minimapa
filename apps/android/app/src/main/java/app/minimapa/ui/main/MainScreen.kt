package app.minimapa.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.minimapa.auth.AuthGateway
import app.minimapa.auth.DemoAuthGateway
import app.minimapa.theme.ForestDeep
import app.minimapa.theme.ForestNight
import app.minimapa.theme.ForestSurface
import app.minimapa.theme.GuildGold
import app.minimapa.theme.GuildGoldDark
import app.minimapa.theme.Parchment
import app.minimapa.theme.ParchmentMuted
import app.minimapa.theme.QuestAmber
import app.minimapa.theme.RiverBlue
import app.minimapa.theme.RoadStone
import app.minimapa.ui.auth.AuthScreen

private enum class AppStage { PORTAL, AUTH, WORLD }
private enum class DemoScreen { MAP, ACTIONS, CREATE_QUEST, CHARACTER, SETTINGS }

private data class DemoQuest(
  val id: String,
  val title: String,
  val route: String,
  val reward: String,
  val distance: String,
)

private val demoQuests =
  listOf(
    DemoQuest("market", "Entrega do Mercado Central", "Centro → Cidade Jardim", "R$ 28", "2,4 km"),
    DemoQuest("books", "Levar uma caixa de livros", "Vila Alemã → Santana", "R$ 34", "4,1 km"),
    DemoQuest("bakery", "Buscar encomenda na padaria", "Saúde → Bela Vista", "R$ 22", "1,8 km"),
  )

@Composable
fun MainScreen(modifier: Modifier = Modifier, authGateway: AuthGateway = DemoAuthGateway) {
  var stage by rememberSaveable { mutableStateOf(AppStage.PORTAL) }
  var screen by rememberSaveable { mutableStateOf(DemoScreen.MAP) }
  var selectedQuestId by rememberSaveable { mutableStateOf<String?>(null) }

  Surface(modifier = modifier.fillMaxSize(), color = ForestNight) {
    when (stage) {
      AppStage.PORTAL -> EntryPortal(onEnter = { stage = AppStage.AUTH })
      AppStage.AUTH ->
        AuthScreen(
          gateway = authGateway,
          onAuthenticated = { stage = AppStage.WORLD },
          onDemoAccount = { stage = AppStage.WORLD },
          onBack = { stage = AppStage.PORTAL },
        )
      AppStage.WORLD ->
        when (screen) {
          DemoScreen.MAP ->
            FantasyMap(
              selectedQuest = demoQuests.firstOrNull { it.id == selectedQuestId },
              onQuestSelected = { selectedQuestId = it.id },
              onCloseQuest = { selectedQuestId = null },
              onOpenActions = { screen = DemoScreen.ACTIONS },
              onOpenCharacter = { screen = DemoScreen.CHARACTER },
              onOpenSettings = { screen = DemoScreen.SETTINGS },
            )
          DemoScreen.ACTIONS ->
            ActionsMenu(
              onBack = { screen = DemoScreen.MAP },
              onCreateQuest = { screen = DemoScreen.CREATE_QUEST },
            )
          DemoScreen.CREATE_QUEST -> CreateQuestDemo(onBack = { screen = DemoScreen.ACTIONS })
          DemoScreen.CHARACTER -> CharacterDemo(onBack = { screen = DemoScreen.MAP })
          DemoScreen.SETTINGS -> SettingsDemo(onBack = { screen = DemoScreen.MAP })
        }
      }
  }
}

@Composable
private fun EntryPortal(onEnter: () -> Unit) {
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(ForestNight, ForestDeep, Color(0xFF154236)))),
  ) {
    Canvas(Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2, size.height * 0.36f)
      drawCircle(GuildGold.copy(alpha = 0.12f), radius = size.minDimension * 0.34f, center = center)
      drawCircle(
        GuildGold.copy(alpha = 0.5f),
        radius = size.minDimension * 0.22f,
        center = center,
        style = Stroke(width = 3f),
      )
    }
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      DemoBadge("PORTAL LOCAL  •  CUSTO ZERO")
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("✦", color = GuildGold, fontSize = 72.sp)
        Text(
          "MINIMAPA",
          color = Parchment,
          fontSize = 38.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 4.sp,
        )
        Text(
          "O mundo está cheio de quests",
          color = ParchmentMuted,
          style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(40.dp))
        Button(
          onClick = onEnter,
          modifier = Modifier.fillMaxWidth().height(58.dp),
          colors = ButtonDefaults.buttonColors(containerColor = GuildGold, contentColor = ForestNight),
          shape = RoundedCornerShape(18.dp),
        ) {
          Text("TOQUE PARA ENTRAR", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("Ambiente seguro • dados simulados • custo zero", color = ParchmentMuted, fontSize = 12.sp)
      }
      Text("Protótipo funcional 0.1", color = ParchmentMuted.copy(alpha = 0.7f), fontSize = 12.sp)
    }
  }
}

@Composable
private fun FantasyMap(
  selectedQuest: DemoQuest?,
  onQuestSelected: (DemoQuest) -> Unit,
  onCloseQuest: () -> Unit,
  onOpenActions: () -> Unit,
  onOpenCharacter: () -> Unit,
  onOpenSettings: () -> Unit,
) {
  Box(Modifier.fillMaxSize().background(ForestDeep)) {
    FantasyMapCanvas()

    Surface(
      modifier = Modifier.align(Alignment.TopCenter).padding(16.dp).fillMaxWidth(),
      color = ForestNight.copy(alpha = 0.94f),
      shape = RoundedCornerShape(18.dp),
      shadowElevation = 8.dp,
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("⌕", color = GuildGold, fontSize = 24.sp)
        Spacer(Modifier.width(10.dp))
        Column {
          Text("Buscar quests e lugares", color = Parchment, fontWeight = FontWeight.SemiBold)
          Text("Rio Claro • piloto local", color = ParchmentMuted, fontSize = 12.sp)
        }
      }
    }

    RoundHudButton(
      label = "NV 1",
      description = "Abrir personagem",
      modifier = Modifier.align(Alignment.TopStart).padding(start = 18.dp, top = 92.dp),
      onClick = onOpenCharacter,
    )
    Column(
      modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      RoundHudButton("!", "Abrir ações", onClick = onOpenActions)
      RoundHudButton("⚙", "Abrir configurações", onClick = onOpenSettings)
      RoundHudButton("◎", "Centralizar mapa", onClick = {})
    }

    QuestPin(
      quest = demoQuests[0],
      modifier = Modifier.align(Alignment.Center).offset(x = (-62).dp, y = (-86).dp),
      onClick = onQuestSelected,
    )
    QuestPin(
      quest = demoQuests[1],
      modifier = Modifier.align(Alignment.Center).offset(x = 84.dp, y = 4.dp),
      onClick = onQuestSelected,
    )
    QuestPin(
      quest = demoQuests[2],
      modifier = Modifier.align(Alignment.Center).offset(x = (-96).dp, y = 112.dp),
      onClick = onQuestSelected,
    )

    DemoBadge(
      text = "MAPA SIMULADO  •  3 QUESTS",
      modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = if (selectedQuest == null) 20.dp else 190.dp),
    )

    if (selectedQuest != null) {
      QuestSheet(
        quest = selectedQuest,
        modifier = Modifier.align(Alignment.BottomCenter),
        onClose = onCloseQuest,
      )
    }
  }
}

@Composable
private fun FantasyMapCanvas() {
  Canvas(Modifier.fillMaxSize().semantics { contentDescription = "Mapa fantasia simulado de Rio Claro" }) {
    drawRect(
      Brush.radialGradient(
        colors = listOf(Color(0xFF31543D), Color(0xFF183B30), ForestDeep),
        center = Offset(size.width * 0.45f, size.height * 0.46f),
        radius = size.maxDimension * 0.72f,
      ),
    )
    val roads =
      listOf(
        Offset(-20f, size.height * 0.30f) to Offset(size.width + 30f, size.height * 0.63f),
        Offset(size.width * 0.18f, -10f) to Offset(size.width * 0.76f, size.height + 20f),
        Offset(-20f, size.height * 0.78f) to Offset(size.width + 20f, size.height * 0.38f),
      )
    roads.forEach { (start, end) ->
      drawLine(RoadStone.copy(alpha = 0.35f), start, end, strokeWidth = 22f, cap = StrokeCap.Round)
      drawLine(ParchmentMuted.copy(alpha = 0.45f), start, end, strokeWidth = 3f, cap = StrokeCap.Round)
    }
    drawLine(
      RiverBlue.copy(alpha = 0.75f),
      Offset(size.width * 0.04f, size.height),
      Offset(size.width * 0.34f, 0f),
      strokeWidth = 34f,
      cap = StrokeCap.Round,
    )
    repeat(26) { index ->
      val x = ((index * 83) % 370).toFloat() / 370f * size.width
      val y = ((index * 137) % 690).toFloat() / 690f * size.height
      drawCircle(Color(0xFF244C35), radius = 10f + (index % 3) * 4f, center = Offset(x, y))
    }
    drawCircle(
      QuestAmber.copy(alpha = 0.12f),
      radius = size.minDimension * 0.42f,
      center = Offset(size.width * 0.5f, size.height * 0.5f),
      style = Stroke(width = 3f),
    )
  }
}

@Composable
private fun QuestPin(quest: DemoQuest, modifier: Modifier, onClick: (DemoQuest) -> Unit) {
  Surface(
    modifier =
      modifier.size(52.dp)
        .semantics { contentDescription = "Quest disponível: ${quest.title}" }
        .clickable { onClick(quest) },
    shape = CircleShape,
    color = QuestAmber,
    contentColor = ForestNight,
    shadowElevation = 10.dp,
    border = androidx.compose.foundation.BorderStroke(3.dp, Parchment),
  ) {
    Box(contentAlignment = Alignment.Center) { Text("!", fontSize = 28.sp, fontWeight = FontWeight.Black) }
  }
}

@Composable
private fun QuestSheet(quest: DemoQuest, modifier: Modifier, onClose: () -> Unit) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = ForestNight,
    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
    shadowElevation = 18.dp,
  ) {
    Column(Modifier.padding(20.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        DemoBadge("QUEST DISPONÍVEL")
        Text("Fechar", color = ParchmentMuted, modifier = Modifier.clickable(onClick = onClose))
      }
      Spacer(Modifier.height(12.dp))
      Text(quest.title, color = Parchment, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      Text(quest.route, color = ParchmentMuted)
      Spacer(Modifier.height(12.dp))
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${quest.distance} • bicicleta ou moto", color = Parchment)
        Text(quest.reward, color = GuildGold, fontWeight = FontWeight.Black, fontSize = 22.sp)
      }
    }
  }
}

@Composable
private fun ActionsMenu(onBack: () -> Unit, onCreateQuest: () -> Unit) {
  FullScreenMenu("Ações", "Escolha o próximo passo", onBack) {
    MenuCard("!", "Postar uma quest", "Crie um rascunho de entrega local", onCreateQuest)
    MenuCard("⌁", "Minhas quests", "Acompanhe rascunhos e jornadas", {})
    MenuCard("♞", "Garagem", "Meios de transporte e favorito", {})
    MenuCard("⌂", "Iniciar uma loja", "Módulo futuro • ainda bloqueado", {}, enabled = false)
  }
}

@Composable
private fun CreateQuestDemo(onBack: () -> Unit) {
  var reward by rememberSaveable { mutableIntStateOf(28) }
  var saved by rememberSaveable { mutableStateOf(false) }
  FullScreenMenu("Nova quest", "Rascunho local de demonstração", onBack) {
    InfoBlock("Modalidade", "Entrega local • pacote pequeno")
    InfoBlock("Origem aproximada", "Centro, Rio Claro")
    InfoBlock("Destino aproximado", "Cidade Jardim, Rio Claro")
    Surface(color = ForestSurface, shape = RoundedCornerShape(18.dp)) {
      Column(Modifier.padding(16.dp)) {
        Text("Recompensa proposta", color = ParchmentMuted)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedButton(onClick = { reward = (reward - 1).coerceAtLeast(1) }) { Text("−") }
          Text(
            "R$ $reward",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = GuildGold,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
          )
          OutlinedButton(onClick = { reward += 1 }) { Text("+") }
        }
      }
    }
    Button(
      onClick = { saved = true },
      modifier = Modifier.fillMaxWidth().height(54.dp),
      colors = ButtonDefaults.buttonColors(containerColor = GuildGold, contentColor = ForestNight),
    ) {
      Text("SALVAR RASCUNHO LOCAL", fontWeight = FontWeight.Bold)
    }
    if (saved) {
      Text(
        "✓ Rascunho simulado salvo nesta tela. Banco real ainda não conectado ao app.",
        color = QuestAmber,
        modifier = Modifier.semantics { contentDescription = "Rascunho local salvo" },
      )
    }
  }
}

@Composable
private fun CharacterDemo(onBack: () -> Unit) {
  FullScreenMenu("Personagem", "Seu aventureiro inicial", onBack) {
    Box(
      modifier =
        Modifier.align(Alignment.CenterHorizontally)
          .size(156.dp)
          .background(Brush.radialGradient(listOf(GuildGoldDark, ForestSurface)), CircleShape)
          .border(2.dp, GuildGold, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Text("♞", fontSize = 76.sp, color = Parchment)
    }
    Text("Aventureiro local", color = Parchment, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Text("Nível 1  •  Avatar ainda não criado", color = ParchmentMuted)
    MenuCard("✦", "Abrir Ateliê", "Planejado para o fluxo de primeiro acesso", {})
  }
}

@Composable
private fun SettingsDemo(onBack: () -> Unit) {
  FullScreenMenu("Configurações", "Preferências desta demonstração", onBack) {
    InfoBlock("Ambiente", "LOCAL • mocks • billing bloqueado")
    InfoBlock("Mapa", "Renderer simulado • nenhuma API externa")
    InfoBlock("Banco", "Migration e RLS validados localmente")
    InfoBlock("Acessibilidade", "Rótulos semânticos e contraste inicial ativos")
  }
}

@Composable
private fun FullScreenMenu(
  title: String,
  subtitle: String,
  onBack: () -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(ForestNight, ForestDeep)))
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text("‹  Voltar ao mapa", color = GuildGold, modifier = Modifier.clickable(onClick = onBack))
    Spacer(Modifier.height(8.dp))
    Text(title, color = Parchment, fontSize = 32.sp, fontWeight = FontWeight.Black)
    Text(subtitle, color = ParchmentMuted)
    HorizontalDivider(color = GuildGoldDark)
    content()
  }
}

@Composable
private fun ColumnScope.MenuCard(
  symbol: String,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  enabled: Boolean = true,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
    color = if (enabled) ForestSurface else ForestSurface.copy(alpha = 0.5f),
    shape = RoundedCornerShape(18.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, GuildGoldDark),
  ) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Text(symbol, color = GuildGold, fontSize = 30.sp, modifier = Modifier.width(46.dp))
      Column {
        Text(title, color = if (enabled) Parchment else ParchmentMuted, fontWeight = FontWeight.Bold)
        Text(subtitle, color = ParchmentMuted, fontSize = 13.sp)
      }
    }
  }
}

@Composable
private fun ColumnScope.InfoBlock(label: String, value: String) {
  Surface(modifier = Modifier.fillMaxWidth(), color = ForestSurface, shape = RoundedCornerShape(16.dp)) {
    Column(Modifier.padding(15.dp)) {
      Text(label, color = ParchmentMuted, fontSize = 12.sp)
      Text(value, color = Parchment, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
private fun RoundHudButton(
  label: String,
  description: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Surface(
    modifier =
      modifier.size(52.dp)
        .semantics { contentDescription = description }
        .clickable(onClick = onClick),
    shape = CircleShape,
    color = ForestNight.copy(alpha = 0.94f),
    contentColor = GuildGold,
    border = androidx.compose.foundation.BorderStroke(1.dp, GuildGoldDark),
    shadowElevation = 7.dp,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(label, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = if (label.length > 2) 11.sp else 22.sp)
    }
  }
}

@Composable
private fun DemoBadge(text: String, modifier: Modifier = Modifier) {
  Surface(modifier = modifier, color = GuildGoldDark.copy(alpha = 0.9f), shape = RoundedCornerShape(50)) {
    Text(
      text,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      color = Parchment,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.7.sp,
    )
  }
}
