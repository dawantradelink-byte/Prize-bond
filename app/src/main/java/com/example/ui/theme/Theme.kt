package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm Cream Canvas Light Theme
private val LightColorScheme = lightColorScheme(
  primary = CoralPrimary,
  onPrimary = Color.White,
  primaryContainer = SurfaceCard,
  onPrimaryContainer = InkPrimary,
  secondary = GoldPrimary,
  onSecondary = RoyalGoldDark,
  secondaryContainer = GoldSuperLight,
  onSecondaryContainer = RoyalGoldDark,
  tertiary = TealAccent,
  onTertiary = Color.White,
  background = CanvasCream,
  onBackground = InkPrimary,
  surface = CanvasCream,
  onSurface = InkPrimary,
  surfaceVariant = SurfaceCard,
  onSurfaceVariant = BodyRegular,
  outline = Hairline,
  outlineVariant = HairlineSoft,
  error = ErrorRed,
  onError = Color.White,
)

// Deep Obsidian / Dark Navy Dark Theme
private val DarkColorScheme = darkColorScheme(
  primary = CoralPrimary,
  onPrimary = Color.White,
  primaryContainer = SurfaceDarkElevated,
  onPrimaryContainer = OnDark,
  secondary = GoldLight,
  onSecondary = RoyalGoldDark,
  secondaryContainer = SurfaceDarkSoft,
  onSecondaryContainer = GoldLight,
  tertiary = TealAccent,
  onTertiary = Color.White,
  background = SurfaceDark,
  onBackground = OnDark,
  surface = SurfaceDark,
  onSurface = OnDark,
  surfaceVariant = SurfaceDarkElevated,
  onSurfaceVariant = OnDarkSoft,
  outline = DarkHairline,
  outlineVariant = SurfaceDarkSoft,
  error = ErrorRed,
  onError = Color.White,
)

@Composable
fun PrizeBondTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content,
  )
}
