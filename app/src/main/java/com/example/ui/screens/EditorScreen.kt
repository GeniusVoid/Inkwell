package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.ExportFormat
import com.example.data.export.ExportHelper
import com.example.data.font.FontItem
import com.example.data.font.FontRegistry
import com.example.data.local.BookEntity
import com.example.data.local.ChapterEntity
import com.example.data.model.AppThemeMode
import com.example.data.model.ChapterStatus
import com.example.ui.components.ChapterStatusBadge
import com.example.ui.components.FontSettingsDialog
import com.example.ui.components.ThemeSwitcherDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
  book: BookEntity?,
  chapter: ChapterEntity?,
  bookTotalWords: Int,
  title: String,
  content: String,
  status: ChapterStatus,
  wordCount: Int,
  charCount: Int,
  isFocusMode: Boolean,
  isSaving: Boolean,
  savedPulse: Boolean,
  lastSavedTimestamp: Long,
  currentTheme: AppThemeMode,
  currentFontId: String,
  currentFontSize: Float,
  currentLineHeight: Float,
  typewriterScrollEnabled: Boolean,
  availableFonts: List<FontItem>,
  onTitleChanged: (String) -> Unit,
  onContentChanged: (String) -> Unit,
  onStatusChanged: (ChapterStatus) -> Unit,
  onToggleFocusMode: () -> Unit,
  onBack: () -> Unit,
  onThemeSelected: (AppThemeMode) -> Unit,
  onFontSelected: (String) -> Unit,
  onFontSizeChanged: (Float) -> Unit,
  onLineHeightChanged: (Float) -> Unit,
  onImportFont: (android.net.Uri) -> Unit,
  onDeleteCustomFont: (String) -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()
  val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

  var showThemeDialog by remember { mutableStateOf(false) }
  var showFontDialog by remember { mutableStateOf(false) }
  var showExportMenu by remember { mutableStateOf(false) }
  var copySuccessPulse by remember { mutableStateOf(false) }

  // Font family resolution
  val resolvedFontFamily = remember(currentFontId) {
    FontRegistry.resolveFontFamily(context, currentFontId)
  }

  // Intercept system back button to flush and exit editor cleanly
  BackHandler {
    if (isFocusMode) {
      onToggleFocusMode()
    } else {
      onBack()
    }
  }

  // Typewriter scrolling effect: center cursor line on content growth when focus mode / typewriter is active
  LaunchedEffect(content.length, isFocusMode, typewriterScrollEnabled) {
    if (typewriterScrollEnabled && isFocusMode) {
      val maxScroll = scrollState.maxValue
      if (maxScroll > 0) {
        scrollState.animateScrollTo((maxScroll * 0.85f).toInt())
      }
    }
  }

  val editorBg = MaterialTheme.colorScheme.background
  val editorTextColor = MaterialTheme.colorScheme.onBackground

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(editorBg)
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = editorBg,
      topBar = {
        AnimatedVisibility(
          visible = !isFocusMode,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          TopAppBar(
            title = {
              Column {
                Text(
                  text = title.ifBlank { "Untitled Chapter" },
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                  ),
                  color = editorTextColor,
                  maxLines = 1
                )
                Text(
                  text = book?.title ?: "Inkwell Novel",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            navigationIcon = {
              IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("editor_back_button")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back to Chapters",
                  tint = MaterialTheme.colorScheme.onSurface
                )
              }
            },
            actions = {
              // Status Badge selector in top bar
              ChapterStatusBadge(
                status = status,
                themeMode = currentTheme,
                onStatusSelected = onStatusChanged,
                modifier = Modifier.padding(end = 4.dp)
              )

              // One-Tap Copy Chapter
              IconButton(
                onClick = {
                  val copied = ExportHelper.copyToClipboard(context, title, content)
                  if (copied) {
                    copySuccessPulse = true
                    scope.launch {
                      delay(2000)
                      copySuccessPulse = false
                    }
                  }
                },
                modifier = Modifier.testTag("copy_chapter_button")
              ) {
                Icon(
                  imageVector = if (copySuccessPulse) Icons.Default.Check else Icons.Default.ContentCopy,
                  contentDescription = "Copy whole chapter",
                  tint = if (copySuccessPulse) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
              }

              // Export Chapter Menu (.md / .txt)
              Box {
                IconButton(
                  onClick = { showExportMenu = true },
                  modifier = Modifier.testTag("export_chapter_menu_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export Chapter",
                    tint = MaterialTheme.colorScheme.onSurface
                  )
                }

                DropdownMenu(
                  expanded = showExportMenu,
                  onDismissRequest = { showExportMenu = false }
                ) {
                  DropdownMenuItem(
                    text = { Text("Export as Markdown (.md)") },
                    onClick = {
                      showExportMenu = false
                      chapter?.let {
                        ExportHelper.shareChapter(
                          context,
                          it.copy(title = title, content = content),
                          ExportFormat.MARKDOWN
                        )
                      }
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Export as Plain Text (.txt)") },
                    onClick = {
                      showExportMenu = false
                      chapter?.let {
                        ExportHelper.shareChapter(
                          context,
                          it.copy(title = title, content = content),
                          ExportFormat.PLAIN_TEXT
                        )
                      }
                    }
                  )
                }
              }

              // Theme Switcher
              IconButton(
                onClick = { showThemeDialog = true },
                modifier = Modifier.testTag("editor_theme_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = "Theme",
                  tint = MaterialTheme.colorScheme.onSurface
                )
              }

              // Typography Settings
              IconButton(
                onClick = { showFontDialog = true },
                modifier = Modifier.testTag("editor_font_button")
              ) {
                Icon(
                  imageVector = Icons.Default.FontDownload,
                  contentDescription = "Font",
                  tint = MaterialTheme.colorScheme.onSurface
                )
              }

              // Focus Mode Toggle
              IconButton(
                onClick = onToggleFocusMode,
                modifier = Modifier.testTag("focus_mode_toggle_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Fullscreen,
                  contentDescription = "Enter Focus Mode",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = editorBg)
          )
        }
      },
      bottomBar = {
        // Quiet, unobtrusive footer stats bar
        AnimatedVisibility(
          visible = !isFocusMode,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Surface(
            color = editorBg,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .navigationBarsPadding(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Left: Chapter metrics
              Text(
                text = "${numberFormat.format(wordCount)} words  ·  ${numberFormat.format(charCount)} chars",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
              )

              // Center: Project Total
              val liveTotalBookWords = (bookTotalWords - (chapter?.wordCount ?: 0)) + wordCount
              Text(
                text = "Book: ${numberFormat.format(liveTotalBookWords)} words",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )

              // Right: Subtle Autosave Pulse Indicator
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (copySuccessPulse) {
                  Text(
                    text = "Copied!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                } else {
                  val pulseAlpha by animateFloatAsState(
                    targetValue = if (savedPulse || isSaving) 1f else 0.4f,
                    label = "pulseAlpha"
                  )
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(
                        if (isSaving) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                      )
                      .alpha(pulseAlpha)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (isSaving) "Saving..." else if (savedPulse) "Saved" else "Autosaved",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    ) { paddingValues ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(
            top = if (isFocusMode) 0.dp else paddingValues.calculateTopPadding(),
            bottom = if (isFocusMode) 0.dp else paddingValues.calculateBottomPadding()
          )
          .imePadding()
          .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp)
            .padding(
              horizontal = if (isFocusMode) 28.dp else 22.dp,
              vertical = if (isFocusMode) 40.dp else 16.dp
            )
        ) {
          // Chapter Title Input field
          BasicTextField(
            value = title,
            onValueChange = onTitleChanged,
            textStyle = TextStyle(
              fontFamily = resolvedFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = (currentFontSize * 1.35f).sp,
              lineHeight = (currentFontSize * 1.6f).sp,
              color = editorTextColor
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
              if (title.isEmpty()) {
                Text(
                  text = "Chapter Title...",
                  style = TextStyle(
                    fontFamily = resolvedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (currentFontSize * 1.35f).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                  )
                )
              }
              innerTextField()
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("chapter_title_input")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Subtle divider line
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Main Chapter Body Editor
          BasicTextField(
            value = content,
            onValueChange = onContentChanged,
            textStyle = TextStyle(
              fontFamily = resolvedFontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = currentFontSize.sp,
              lineHeight = (currentFontSize * currentLineHeight).sp,
              letterSpacing = 0.3.sp,
              color = editorTextColor
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
              if (content.isEmpty()) {
                Text(
                  text = "Once upon a time in a forgotten world...\n\nBegin typing your chapter here.",
                  style = TextStyle(
                    fontFamily = resolvedFontFamily,
                    fontSize = currentFontSize.sp,
                    lineHeight = (currentFontSize * currentLineHeight).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                  )
                )
              }
              innerTextField()
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("chapter_content_editor")
          )

          // Extra bottom padding for comfortable typewriter scrolling
          Spacer(modifier = Modifier.height(if (isFocusMode) 320.dp else 120.dp))
        }
      }
    }

    // Floating Focus Mode Exit Pill & Quiet Focus Word Count
    if (isFocusMode) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
          .navigationBarsPadding()
          .testTag("focus_exit_pill")
      ) {
        Row(
          modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable(onClick = onToggleFocusMode),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.FullscreenExit,
            contentDescription = "Exit Focus Mode",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${numberFormat.format(wordCount)} words",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }

  // Dialogs
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
}
