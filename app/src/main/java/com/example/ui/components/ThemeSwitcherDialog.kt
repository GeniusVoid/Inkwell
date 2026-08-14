package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppThemeMode
import com.example.ui.theme.IvoryLightBackground
import com.example.ui.theme.SepiaBackground
import com.example.ui.theme.SepiaPrimary
import com.example.ui.theme.SoftDarkBackground
import com.example.ui.theme.SoftDarkPrimary
import com.example.ui.theme.TrueDarkBackground
import com.example.ui.theme.TrueDarkPrimary

@Composable
fun ThemeSwitcherDialog(
  currentTheme: AppThemeMode,
  onThemeSelected: (AppThemeMode) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Palette,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Reading & Writing Theme",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        AppThemeMode.entries.forEach { mode ->
          val isSelected = mode == currentTheme
          val (bgSample, accentSample, textSample) = when (mode) {
            AppThemeMode.PAPER_SEPIA -> Triple(SepiaBackground, SepiaPrimary, Color(0xFF2E2117))
            AppThemeMode.SOFT_DARK -> Triple(SoftDarkBackground, SoftDarkPrimary, Color(0xFFEDE7DE))
            AppThemeMode.TRUE_DARK -> Triple(TrueDarkBackground, TrueDarkPrimary, Color(0xFFD8D9DE))
            AppThemeMode.IVORY_LIGHT -> Triple(IvoryLightBackground, Color(0xFF6B4226), Color(0xFF1C1A17))
          }

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onThemeSelected(mode)
                onDismiss()
              }
              .testTag("theme_option_${mode.name.lowercase()}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                // Color preview badge
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgSample)
                    .clip(CircleShape)
                ) {
                  Box(
                    modifier = Modifier
                      .size(16.dp)
                      .clip(CircleShape)
                      .background(accentSample)
                      .align(Alignment.Center)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = mode.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = mode.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}
