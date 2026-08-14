package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupVersionInfo
import com.example.data.font.FontItem
import com.example.data.local.BookWithStats
import com.example.data.model.AppThemeMode
import com.example.ui.components.BackupHistoryModal
import com.example.ui.components.CreateBookDialog
import com.example.ui.components.FontSettingsDialog
import com.example.ui.components.ThemeSwitcherDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
  books: List<BookWithStats>,
  lastBackupTime: Long,
  backupVersions: List<BackupVersionInfo>,
  isBackupRunning: Boolean,
  backupMessage: String?,
  isGoogleConnected: Boolean,
  googleAccountEmail: String?,
  googleAccountDisplayName: String?,
  currentTheme: AppThemeMode,
  currentFontId: String,
  currentFontSize: Float,
  currentLineHeight: Float,
  availableFonts: List<FontItem>,
  onBookSelected: (Long) -> Unit,
  onCreateBook: (title: String, color: String, label: String, synopsis: String) -> Unit,
  onConnectGoogle: () -> Unit,
  onDisconnectGoogle: () -> Unit,
  onBackupNow: () -> Unit,
  onRestoreBackup: (String) -> Unit,
  onThemeSelected: (AppThemeMode) -> Unit,
  onFontSelected: (String) -> Unit,
  onFontSizeChanged: (Float) -> Unit,
  onLineHeightChanged: (Float) -> Unit,
  onImportFont: (android.net.Uri) -> Unit,
  onDeleteCustomFont: (String) -> Unit,
  onClearBackupMessage: () -> Unit
) {
  var showCreateBookDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }
  var showFontDialog by remember { mutableStateOf(false) }
  var showBackupModal by remember { mutableStateOf(false) }

  val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            // App Emblem
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(34.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.AutoStories,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Inkwell",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontFamily = FontFamily.Serif,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = "Author's Sanctuary",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = { showThemeDialog = true },
            modifier = Modifier.testTag("theme_switcher_button")
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = "Switch Theme",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(
            onClick = { showFontDialog = true },
            modifier = Modifier.testTag("font_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.FontDownload,
              contentDescription = "Typography Settings",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          IconButton(
            onClick = { showBackupModal = true },
            modifier = Modifier.testTag("backup_history_button")
          ) {
            Icon(
              imageVector = Icons.Default.CloudDone,
              contentDescription = "Backup Manager",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateBookDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("create_book_fab")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = "New Novel")
          Spacer(modifier = Modifier.width(6.dp))
          Text("New Novel", fontWeight = FontWeight.Bold)
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(paddingValues)
    ) {
      // Manual Backup Quick Status Strip
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clickable { showBackupModal = true }
          .testTag("backup_status_strip")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.CloudUpload,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              val backupStr = if (lastBackupTime > 0) {
                "Last backup: " + SimpleDateFormat("MMM dd · HH:mm", Locale.getDefault()).format(Date(lastBackupTime))
              } else {
                "Offline · No manual backup saved yet"
              }
              Text(
                text = backupStr,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Manual Google Drive version snapshots",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          OutlinedButton(
            onClick = onBackupNow,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("quick_backup_now_button")
          ) {
            Text(
              text = if (isBackupRunning) "Saving..." else "Backup Now",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Books Section Title
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Your Novel Projects (${books.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        val grandTotalWords = books.sumOf { it.totalWordCount }
        Text(
          text = "${numberFormat.format(grandTotalWords)} words total",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }

      if (books.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.MenuBook,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Your library is waiting for its first story.",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap '+ New Novel' to begin crafting your chapters.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 165.dp),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(books) { book ->
            BookCoverCard(
              book = book,
              numberFormat = numberFormat,
              onClick = { onBookSelected(book.id) }
            )
          }
        }
      }
    }
  }

  // Dialogs
  if (showCreateBookDialog) {
    CreateBookDialog(
      onConfirm = onCreateBook,
      onDismiss = { showCreateBookDialog = false }
    )
  }

  if (showThemeDialog) {
    ThemeSwitcherDialog(
      currentTheme = currentTheme,
      onThemeSelected = onThemeSelected,
      onDismiss = { showThemeDialog = false }
    )
  }

  if (showFontDialog) {
    FontSettingsDialog(
      currentFontId = currentFontId,
      currentFontSize = currentFontSize,
      currentLineHeight = currentLineHeight,
      availableFonts = availableFonts,
      onFontSelected = onFontSelected,
      onFontSizeChanged = onFontSizeChanged,
      onLineHeightChanged = onLineHeightChanged,
      onImportFont = onImportFont,
      onDeleteCustomFont = onDeleteCustomFont,
      onDismiss = { showFontDialog = false }
    )
  }

  if (showBackupModal) {
    BackupHistoryModal(
      lastBackupTime = lastBackupTime,
      backupVersions = backupVersions,
      isBackupRunning = isBackupRunning,
      backupMessage = backupMessage,
      isGoogleConnected = isGoogleConnected,
      googleAccountEmail = googleAccountEmail,
      googleAccountDisplayName = googleAccountDisplayName,
      onConnectGoogle = onConnectGoogle,
      onDisconnectGoogle = onDisconnectGoogle,
      onBackupNow = onBackupNow,
      onRestoreVersion = onRestoreBackup,
      onDismiss = {
        showBackupModal = false
        onClearBackupMessage()
      }
    )
  }
}

@Composable
fun BookCoverCard(
  book: BookWithStats,
  numberFormat: NumberFormat,
  onClick: () -> Unit
) {
  val coverColor = remember(book.coverColor) {
    try {
      Color(android.graphics.Color.parseColor(book.coverColor))
    } catch (e: Exception) {
      Color(0xFF8B1E2E)
    }
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .testTag("book_card_${book.id}")
  ) {
    Column {
      // Book Spine & Header with rich gradient
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .background(
            Brush.verticalGradient(
              colors = listOf(
                coverColor,
                coverColor.copy(alpha = 0.85f)
              )
            )
          )
          .padding(12.dp)
      ) {
        // Spine left shadow accent
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.25f),
                  Color.Transparent
                ),
                startX = 0f,
                endX = 24f
              )
            )
        )

        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          // Genre Label / Badge
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.35f),
            modifier = Modifier.align(Alignment.End)
          ) {
            Text(
              text = book.coverLabel.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
              ),
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          // Title on Cover
          Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontFamily = FontFamily.Serif,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              lineHeight = 20.sp
            ),
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Book Details / Stats
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "${book.chapterCount} chapters",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${numberFormat.format(book.totalWordCount)} words",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }

        if (book.synopsis.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = book.synopsis,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}
