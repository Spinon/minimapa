package app.minimapa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.minimapa.auth.SupabaseAuthGateway
import app.minimapa.theme.MinimapaTheme

class MainActivity : ComponentActivity() {
  private val authGateway by lazy { SupabaseAuthGateway.fromBuildConfig() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    authGateway.handleDeepLink(intent)

    enableEdgeToEdge()
    setContent {
      MinimapaTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(authGateway = authGateway)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    authGateway.handleDeepLink(intent)
  }
}
