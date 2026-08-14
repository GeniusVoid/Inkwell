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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.BookEntity
import com.example.data.model.PRESET_COVER_PALETTES

@Composable
fun CreateBookDialog(
  onConfirm: (title: String, coverColor: String, coverLabel: String, synopsis: String) -> Unit,
  onDismiss: () -> Unit
) {
  var title by remember { mutableStateOf("") }
  var selectedColorHex by remember { mutableStateOf(PRESET_COVER_PALETTES.first().hexColor) }
  var label by remember { mutableStateOf("NOVEL") }
  var synopsis by remember { mutableStateOf("") }

  val presetLabels = listOf("NOVEL", "SCI-FI", "FANTASY", "LITRPG", "ROMANCE", "MYSTERY", "WUXIA", "DRAFT")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.AutoStories,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "New Novel Project",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Novel Title") },
          placeholder = { Text("e.g., The Silent Archon") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_book_title_input")
        )

        // Cover Color Picker
        Text(
          text = "Cover Color",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(PRESET_COVER_PALETTES) { palette ->
            val isSelected = palette.hexColor == selectedColorHex
            val parsedColor = Color(android.graphics.Color.parseColor(palette.hexColor))
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(parsedColor)
                .clickable { selectedColorHex = palette.hexColor }
                .then(
                  if (isSelected) Modifier.background(parsedColor) else Modifier
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        }

        // Genre / Label tags
        Text(
          text = "Spine / Badge Tag",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(presetLabels) { tag ->
            val isSelected = label == tag
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
              ),
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { label = tag }
            ) {
              Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = synopsis,
          onValueChange = { synopsis = it },
          label = { Text("Synopsis / Premise (Optional)") },
          placeholder = { Text("Logline, themes, character notes...") },
          minLines = 2,
          maxLines = 4,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_book_synopsis_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onConfirm(title, selectedColorHex, label, synopsis)
            onDismiss()
          }
        },
        enabled = title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.testTag("create_book_confirm_button")
      ) {
        Text("Create Book")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun EditBookDialog(
  book: BookEntity,
  onConfirm: (BookEntity) -> Unit,
  onDeleteBook: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  var title by remember { mutableStateOf(book.title) }
  var selectedColorHex by remember { mutableStateOf(book.coverColor) }
  var label by remember { mutableStateOf(book.coverLabel) }
  var synopsis by remember { mutableStateOf(book.synopsis) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  val presetLabels = listOf("NOVEL", "SCI-FI", "FANTASY", "LITRPG", "ROMANCE", "MYSTERY", "WUXIA", "DRAFT")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Edit Project Info",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Novel Title") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // Cover Color Picker
        Text(
          text = "Cover Color",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(PRESET_COVER_PALETTES) { palette ->
            val isSelected = palette.hexColor == selectedColorHex
            val parsedColor = Color(android.graphics.Color.parseColor(palette.hexColor))
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(parsedColor)
                .clickable { selectedColorHex = palette.hexColor },
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        }

        // Genre / Label tags
        Text(
          text = "Spine / Badge Tag",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(presetLabels) { tag ->
            val isSelected = label == tag
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
              ),
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { label = tag }
            ) {
              Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = synopsis,
          onValueChange = { synopsis = it },
          label = { Text("Synopsis / Premise") },
          minLines = 2,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(
          onClick = { showDeleteConfirm = true },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete Book Project & All Chapters")
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onConfirm(
              book.copy(
                title = title,
                coverColor = selectedColorHex,
                coverLabel = label,
                synopsis = synopsis
              )
            )
            onDismiss()
          }
        },
        enabled = title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("Delete Novel Project?") },
      text = { Text("Are you sure you want to delete '${book.title}' and all its chapters? This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteBook(book.id)
            showDeleteConfirm = false
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
