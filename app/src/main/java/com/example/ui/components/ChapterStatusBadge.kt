package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.data.model.ChapterStatus
import com.example.ui.theme.getStatusColors

@Composable
fun ChapterStatusBadge(
  status: ChapterStatus,
  themeMode: AppThemeMode,
  modifier: Modifier = Modifier,
  onStatusSelected: ((ChapterStatus) -> Unit)? = null
) {
  val (bgColor, textColor, borderColor) = getStatusColors(status, themeMode)
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = bgColor,
      border = BorderStroke(1.dp, borderColor),
      modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .then(
          if (onStatusSelected != null) {
            Modifier
              .clickable { expanded = true }
              .testTag("status_badge_${status.code.lowercase()}")
          } else Modifier
        )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        val icon = when (status) {
          ChapterStatus.DRAFT -> Icons.Default.HourglassEmpty
          ChapterStatus.EDITING -> Icons.Default.Edit
          ChapterStatus.DONE -> Icons.Default.Check
        }
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = textColor,
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = " " + status.displayName,
          color = textColor,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
          )
        )
      }
    }

    if (onStatusSelected != null) {
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
      ) {
        ChapterStatus.entries.forEach { option ->
          val (optBg, optText, _) = getStatusColors(option, themeMode)
          DropdownMenuItem(
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(optText)
                )
                Text(
                  text = "  " + option.displayName,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = if (option == status) FontWeight.Bold else FontWeight.Normal
                )
              }
            },
            onClick = {
              onStatusSelected(option)
              expanded = false
            }
          )
        }
      }
    }
  }
}
