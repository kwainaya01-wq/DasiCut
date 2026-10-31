package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DesiCutDarkColorScheme = darkColorScheme(
  primary = EmeraldPrimary,
  onPrimary = CharcoalBackground,
  primaryContainer = EmeraldDark,
  onPrimaryContainer = TextPrimary,
  secondary = GoldAccent,
  onSecondary = CharcoalBackground,
  secondaryContainer = CharcoalCardSelected,
  onSecondaryContainer = GoldAccent,
  tertiary = CyanAccent,
  onTertiary = CharcoalBackground,
  background = CharcoalBackground,
  onBackground = TextPrimary,
  surface = CharcoalSurface,
  onSurface = TextPrimary,
  surfaceVariant = CharcoalSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CharcoalBorder
)

@Composable
fun DesiCutTheme(
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = CharcoalBackground.toArgb()
        window.navigationBarColor = CharcoalBackground.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = DesiCutDarkColorScheme,
    typography = Typography,
    content = content
  )
}
