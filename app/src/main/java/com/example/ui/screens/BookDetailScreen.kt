package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.ExportFormat
import com.example.data.export.ExportHelper
import com.example.data.font.FontItem
import com.example.data.local.BookEntity
import com.example.data.local.ChapterEntity
import com.example.data.model.AppThemeMode
import com.example.data.model.ChapterStatus
import com.example.data.model.ViewLayoutMode
import com.example.ui.components.ChapterStatusBadge
import com.example.ui.components.EditBookDialog
import com.example.ui.components.FontSettingsDialog
import com.example.ui.components.ThemeSwitcherDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
  book: BookEntity,
  chapters: List<ChapterEntity>,
  totalWordCount: Int,
  viewLayoutMode: ViewLayoutMode,
  currentTheme: AppThemeMode,
  currentFontId: String,
  currentFontSize: Float,
  currentLineHeight: Float,
  availableFonts: List<FontItem>,
  onBack: () -> Unit,
  onChapterSelected: (Long) -> Unit,
  onCreateChapter: (Long, String?) -> Unit,
  onDeleteChapter: (Long) -> Unit,
  onStatusChanged: (ChapterEntity, ChapterStatus) -> Unit,
  onMoveUp: (Int) -> Unit,
  onMoveDown: (Int) -> Unit,
  onToggleViewLayout: (ViewLayoutMode) -> Unit,
  onUpdateBook: (BookEntity) -> Unit,
  onDeleteBook: (Long) -> Unit,
  onThemeSelected: (AppThemeMode) -> Unit,
  onFontSelected: (String) -> Unit,
  onFontSizeChanged: (Float) -> Unit,
  onLineHeightChanged: (Float) -> Unit,
  onImportFont: (android.net.Uri) -> Unit,
  onDeleteCustomFont: (String) -> Unit
) {
  val context = LocalContext.current
  val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

  var isReorderMode by remember { mutableStateOf(false) }
  var showNewChapterDialog by remember { mutableStateOf(false) }
  var showEditBookDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }
  var showFontDialog by remember { mutableStateOf(false) }
  var showExportMenu by remember { mutableStateOf(false) }
  var chapterToDelete by remember { mutableStateOf<ChapterEntity?>(null) }

  val coverColor = remember(book.coverColor) {
    try {
      Color(android.graphics.Color.parseColor(book.coverColor))
    } catch (e: Exception) {
      Color(0xFF8B1E2E)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = book.title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onBackground,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${chapters.size} chapters · ${numberFormat.format(totalWordCount)} words",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("book_detail_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Library",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        actions = {
          // Toggle View (Grid vs List)
          IconButton(
            onClick = {
              val newMode = if (viewLayoutMode == ViewLayoutMode.GRID) ViewLayoutMode.LIST else ViewLayoutMode.GRID
              onToggleViewLayout(newMode)
            },
            modifier = Modifier.testTag("toggle_view_mode_button")
          ) {
            Icon(
              imageVector = if (viewLayoutMode == ViewLayoutMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
              contentDescription = "Toggle Grid/List",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // Reorder mode toggle
          IconButton(
            onClick = { isReorderMode = !isReorderMode },
            modifier = Modifier.testTag("toggle_reorder_mode_button")
          ) {
            Icon(
              imageVector = Icons.Default.DriveFileMove,
              contentDescription = "Reorder Chapters",
              tint = if (isReorderMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
          }

          // Export full book
          Box {
            IconButton(
              onClick = { showExportMenu = true },
              modifier = Modifier.testTag("export_book_menu_button")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Export Novel",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }

            DropdownMenu(
              expanded = showExportMenu,
              onDismissRequest = { showExportMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("Export Full Novel as Markdown (.md)") },
                onClick = {
                  showExportMenu = false
                  ExportHelper.shareFullBook(context, book, chapters, ExportFormat.MARKDOWN)
                }
              )
              DropdownMenuItem(
                text = { Text("Export Full Novel as Plain Text (.txt)") },
                onClick = {
                  showExportMenu = false
                  ExportHelper.shareFullBook(context, book, chapters, ExportFormat.PLAIN_TEXT)
                }
              )
            }
          }

          IconButton(
            onClick = { showEditBookDialog = true },
            modifier = Modifier.testTag("edit_book_button")
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Book Settings",
              tint = MaterialTheme.colorScheme.onSurface
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
        onClick = { showNewChapterDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("new_chapter_fab")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Chapter")
          Spacer(modifier = Modifier.width(6.dp))
          Text("New Chapter", fontWeight = FontWeight.Bold)
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
      // Reorder Mode Alert Banner
      AnimatedVisibility(visible = isReorderMode) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Reorder Mode: Use arrows to arrange chapters",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { isReorderMode = false }) {
              Text("Done", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      if (chapters.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "No chapters written yet.",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap '+ New Chapter' below to start writing.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        if (viewLayoutMode == ViewLayoutMode.GRID) {
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            itemsIndexed(chapters, key = { _, ch -> ch.id }) { index, chapter ->
              ChapterGridCard(
                chapter = chapter,
                index = index,
                totalChapters = chapters.size,
                isReorderMode = isReorderMode,
                themeMode = currentTheme,
                numberFormat = numberFormat,
                onClick = { onChapterSelected(chapter.id) },
                onStatusChange = { newStatus -> onStatusChanged(chapter, newStatus) },
                onMoveUp = { onMoveUp(index) },
                onMoveDown = { onMoveDown(index) },
                onDelete = { chapterToDelete = chapter }
              )
            }
          }
        } else {
          LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            itemsIndexed(chapters, key = { _, ch -> ch.id }) { index, chapter ->
              ChapterListRow(
                chapter = chapter,
                index = index,
                totalChapters = chapters.size,
                isReorderMode = isReorderMode,
                themeMode = currentTheme,
                numberFormat = numberFormat,
                onClick = { onChapterSelected(chapter.id) },
                onStatusChange = { newStatus -> onStatusChanged(chapter, newStatus) },
                onMoveUp = { onMoveUp(index) },
                onMoveDown = { onMoveDown(index) },
                onDelete = { chapterToDelete = chapter }
              )
            }
          }
        }
      }
    }
  }

  // Quick Chapter Creation Dialog
  if (showNewChapterDialog) {
    var titleInput by remember { mutableStateOf("Chapter ${chapters.size + 1}") }
    AlertDialog(
      onDismissRequest = { showNewChapterDialog = false },
      title = { Text("Add Chapter") },
      text = {
        OutlinedTextField(
          value = titleInput,
          onValueChange = { titleInput = it },
          label = { Text("Chapter Title") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_chapter_title_input")
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            onCreateChapter(book.id, titleInput.ifBlank { "Chapter ${chapters.size + 1}" })
            showNewChapterDialog = false
          },
          modifier = Modifier.testTag("confirm_add_chapter_button")
        ) {
          Text("Create & Write")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewChapterDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Chapter Confirmation
  chapterToDelete?.let { ch ->
    AlertDialog(
      onDismissRequest = { chapterToDelete = null },
      title = { Text("Delete Chapter?") },
      text = { Text("Are you sure you want to delete '${ch.title}'? This action cannot be undone.") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteChapter(ch.id)
            chapterToDelete = null
          },
          modifier = Modifier.testTag("confirm_delete_chapter_button")
        ) {
          Text("Delete", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { chapterToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Edit Book Dialog
  if (showEditBookDialog) {
    EditBookDialog(
      book = book,
      onConfirm = onUpdateBook,
      onDeleteBook = {
        onDeleteBook(it)
        onBack()
      },
      onDismiss = { showEditBookDialog = false }
    )
  }
}

@Composable
fun ChapterGridCard(
  chapter: ChapterEntity,
  index: Int,
  totalChapters: Int,
  isReorderMode: Boolean,
  themeMode: AppThemeMode,
  numberFormat: NumberFormat,
  onClick: () -> Unit,
  onStatusChange: (ChapterStatus) -> Unit,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
  onDelete: () -> Unit
) {
  val currentStatus = ChapterStatus.fromCode(chapter.status)

  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .testTag("chapter_grid_item_${chapter.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "#${index + 1}",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        ChapterStatusBadge(
          status = currentStatus,
          themeMode = themeMode,
          onStatusSelected = onStatusChange
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = chapter.title.ifBlank { "Untitled Chapter" },
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontFamily = FontFamily.Serif
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${numberFormat.format(chapter.wordCount)} words",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isReorderMode) {
          Row {
            if (index > 0) {
              IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
              }
            }
            if (index < totalChapters - 1) {
              IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ChapterListRow(
  chapter: ChapterEntity,
  index: Int,
  totalChapters: Int,
  isReorderMode: Boolean,
  themeMode: AppThemeMode,
  numberFormat: NumberFormat,
  onClick: () -> Unit,
  onStatusChange: (ChapterStatus) -> Unit,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
  onDelete: () -> Unit
) {
  val currentStatus = ChapterStatus.fromCode(chapter.status)

  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .testTag("chapter_list_item_${chapter.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(28.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "${index + 1}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = chapter.title.ifBlank { "Untitled Chapter" },
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontFamily = FontFamily.Serif
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${numberFormat.format(chapter.wordCount)} words · ${chapter.charCount} chars",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        ChapterStatusBadge(
          status = currentStatus,
          themeMode = themeMode,
          onStatusSelected = onStatusChange
        )

        if (isReorderMode) {
          Spacer(modifier = Modifier.width(8.dp))
          Row {
            if (index > 0) {
              IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
              }
            }
            if (index < totalChapters - 1) {
              IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }
}
