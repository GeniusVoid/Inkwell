package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.font.FontItem
import com.example.data.font.FontRegistry

@Composable
fun FontSettingsDialog(
  currentFontId: String,
  currentFontSize: Float,
  currentLineHeight: Float,
  availableFonts: List<FontItem>,
  onFontSelected: (String) -> Unit,
  onFontSizeChanged: (Float) -> Unit,
  onLineHeightChanged: (Float) -> Unit,
  onImportFont: (Uri) -> Unit,
  onDeleteCustomFont: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var localFontSize by remember { mutableFloatStateOf(currentFontSize) }
  var localLineHeight by remember { mutableFloatStateOf(currentLineHeight) }

  val fontPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    uri?.let { onImportFont(it) }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.FontDownload,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Typography & Sizing",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Size and spacing sliders
        item {
          Text(
            text = "Text Size: ${localFontSize.toInt()} sp",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Slider(
            value = localFontSize,
            onValueChange = {
              localFontSize = it
              onFontSizeChanged(it)
            },
            valueRange = 14f..28f,
            steps = 13,
            colors = SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("font_size_slider")
          )
        }

        item {
          val lineDisplay = String.format("%.2f", localLineHeight)
          Text(
            text = "Line Height Multiplier: ${lineDisplay}x",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Slider(
            value = localLineHeight,
            onValueChange = {
              localLineHeight = it
              onLineHeightChanged(it)
            },
            valueRange = 1.3f..2.2f,
            steps = 8,
            colors = SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("line_height_slider")
          )
        }

        // Live Preview Box
        item {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            val previewFont = remember(currentFontId) {
              FontRegistry.resolveFontFamily(context, currentFontId)
            }
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Preview",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "The quick brown fox jumps over the lazy dog. In the silence of the night, words breathe life.",
                fontFamily = previewFont,
                fontSize = localFontSize.sp,
                lineHeight = (localFontSize * localLineHeight).sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Font Families Header + Import Button
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Choose Font Family",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
            OutlinedButton(
              onClick = {
                fontPickerLauncher.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "*/*"))
              },
              modifier = Modifier.testTag("import_font_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Import .ttf / .otf", style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        // Font list
        items(availableFonts) { fontItem ->
          val isSelected = fontItem.id == currentFontId
          val itemFont = remember(fontItem.id) {
            FontRegistry.resolveFontFamily(context, fontItem.id)
          }

          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onFontSelected(fontItem.id) }
              .testTag("font_item_${fontItem.id}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = fontItem.name,
                    fontFamily = itemFont,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Text(
                  text = fontItem.category,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (fontItem.isCustom) {
                IconButton(
                  onClick = { onDeleteCustomFont(fontItem.id) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Font",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.testTag("close_font_settings_button")
      ) {
        Text("Done")
      }
    }
  )
}
