package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemeMode
import com.example.data.model.ChapterStatus

val SepiaColorScheme: ColorScheme = lightColorScheme(
  primary = SepiaPrimary,
  onPrimary = SepiaOnPrimary,
  primaryContainer = SepiaSurfaceVariant,
  onPrimaryContainer = SepiaOnBackground,
  secondary = InkwellDarkBronze,
  onSecondary = Color.White,
  background = SepiaBackground,
  onBackground = SepiaOnBackground,
  surface = SepiaSurface,
  onSurface = SepiaOnSurface,
  surfaceVariant = SepiaSurfaceVariant,
  onSurfaceVariant = SepiaMutedText,
  outline = SepiaOutline
)

val SoftDarkColorScheme: ColorScheme = darkColorScheme(
  primary = SoftDarkPrimary,
  onPrimary = SoftDarkOnPrimary,
  primaryContainer = SoftDarkSurfaceVariant,
  onPrimaryContainer = SoftDarkOnBackground,
  secondary = InkwellGold,
  onSecondary = SoftDarkBackground,
  background = SoftDarkBackground,
  onBackground = SoftDarkOnBackground,
  surface = SoftDarkSurface,
  onSurface = SoftDarkOnSurface,
  surfaceVariant = SoftDarkSurfaceVariant,
  onSurfaceVariant = SoftDarkMutedText,
  outline = SoftDarkOutline
)

val TrueDarkColorScheme: ColorScheme = darkColorScheme(
  primary = TrueDarkPrimary,
  onPrimary = TrueDarkOnPrimary,
  primaryContainer = TrueDarkSurfaceVariant,
  onPrimaryContainer = TrueDarkOnBackground,
  secondary = InkwellGold,
  onSecondary = Color.Black,
  background = TrueDarkBackground,
  onBackground = TrueDarkOnBackground,
  surface = TrueDarkSurface,
  onSurface = TrueDarkOnSurface,
  surfaceVariant = TrueDarkSurfaceVariant,
  onSurfaceVariant = TrueDarkMutedText,
  outline = TrueDarkOutline
)

val IvoryLightColorScheme: ColorScheme = lightColorScheme(
  primary = IvoryLightPrimary,
  onPrimary = IvoryLightOnPrimary,
  primaryContainer = IvoryLightSurfaceVariant,
  onPrimaryContainer = IvoryLightOnBackground,
  secondary = InkwellDarkBronze,
  onSecondary = Color.White,
  background = IvoryLightBackground,
  onBackground = IvoryLightOnBackground,
  surface = IvoryLightSurface,
  onSurface = IvoryLightOnSurface,
  surfaceVariant = IvoryLightSurfaceVariant,
  onSurfaceVariant = IvoryLightMutedText,
  outline = IvoryLightOutline
)

@Composable
fun getStatusColors(status: ChapterStatus, themeMode: AppThemeMode): Triple<Color, Color, Color> {
  // Returns (Background, Text, Border)
  val isDark = themeMode == AppThemeMode.SOFT_DARK || themeMode == AppThemeMode.TRUE_DARK
  return if (isDark) {
    when (status) {
      ChapterStatus.DRAFT -> Triple(StatusDraftBgDark, StatusDraftTextDark, StatusDraftBorderDark)
      ChapterStatus.EDITING -> Triple(StatusEditingBgDark, StatusEditingTextDark, StatusEditingBorderDark)
      ChapterStatus.DONE -> Triple(StatusDoneBgDark, StatusDoneTextDark, StatusDoneBorderDark)
    }
  } else {
    when (status) {
      ChapterStatus.DRAFT -> Triple(StatusDraftBg, StatusDraftText, StatusDraftBorder)
      ChapterStatus.EDITING -> Triple(StatusEditingBg, StatusEditingText, StatusEditingBorder)
      ChapterStatus.DONE -> Triple(StatusDoneBg, StatusDoneText, StatusDoneBorder)
    }
  }
}

@Composable
fun InkwellTheme(
  themeMode: AppThemeMode = AppThemeMode.PAPER_SEPIA,
  content: @Composable () -> Unit
) {
  val colorScheme = when (themeMode) {
    AppThemeMode.PAPER_SEPIA -> SepiaColorScheme
    AppThemeMode.SOFT_DARK -> SoftDarkColorScheme
    AppThemeMode.TRUE_DARK -> TrueDarkColorScheme
    AppThemeMode.IVORY_LIGHT -> IvoryLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
