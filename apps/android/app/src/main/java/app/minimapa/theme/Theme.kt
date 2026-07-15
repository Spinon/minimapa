package app.minimapa.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MinimapaColorScheme =
  darkColorScheme(
    primary = GuildGold,
    onPrimary = ForestNight,
    secondary = ParchmentMuted,
    onSecondary = ForestNight,
    background = ForestNight,
    onBackground = Parchment,
    surface = ForestSurface,
    onSurface = Parchment,
    error = DangerRed,
  )

@Composable
fun MinimapaTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = MinimapaColorScheme, typography = Typography, content = content)
}
