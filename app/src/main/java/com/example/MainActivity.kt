package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppThemeMode
import com.example.ui.screens.BookDetailScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.InkwellTheme
import com.example.ui.viewmodel.InkwellViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
  private val viewModel: InkwellViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val globalTheme by viewModel.globalTheme.collectAsStateWithLifecycle()
      val globalFont by viewModel.globalFont.collectAsStateWithLifecycle()
      val globalFontSize by viewModel.globalFontSize.collectAsStateWithLifecycle()
      val globalLineHeight by viewModel.globalLineHeight.collectAsStateWithLifecycle()
      val chapterViewMode by viewModel.chapterViewMode.collectAsStateWithLifecycle()
      val typewriterScrollEnabled by viewModel.typewriterScrollEnabled.collectAsStateWithLifecycle()
      val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()
      val backupVersions by viewModel.backupVersions.collectAsStateWithLifecycle()
      val isBackupRunning by viewModel.isBackupRunning.collectAsStateWithLifecycle()
      val backupMessage by viewModel.backupMessage.collectAsStateWithLifecycle()
      val isGoogleConnected by viewModel.isGoogleConnected.collectAsStateWithLifecycle()
      val googleAccountEmail by viewModel.googleAccountEmail.collectAsStateWithLifecycle()
      val googleAccountDisplayName by viewModel.googleAccountDisplayName.collectAsStateWithLifecycle()

      val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
      ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
          val account = task.getResult(ApiException::class.java)
          viewModel.updateGoogleAccount(account)
        } catch (e: Exception) {
          viewModel.onGoogleSignInFailed(e.localizedMessage ?: "Sign-in cancelled or failed")
        }
      }

      val books by viewModel.booksWithStats.collectAsStateWithLifecycle()
      val selectedBookId by viewModel.selectedBookId.collectAsStateWithLifecycle()
      val currentBook by viewModel.currentBook.collectAsStateWithLifecycle()
      val currentBookChapters by viewModel.currentBookChapters.collectAsStateWithLifecycle()
      val currentBookTotalWords by viewModel.currentBookTotalWords.collectAsStateWithLifecycle()

      val activeChapterId by viewModel.activeChapterId.collectAsStateWithLifecycle()
      val activeChapter by viewModel.activeChapter.collectAsStateWithLifecycle()

      val editorTitle by viewModel.editorTitle.collectAsStateWithLifecycle()
      val editorContent by viewModel.editorContent.collectAsStateWithLifecycle()
      val editorStatus by viewModel.editorStatus.collectAsStateWithLifecycle()
      val chapterWordCount by viewModel.chapterWordCount.collectAsStateWithLifecycle()
      val chapterCharCount by viewModel.chapterCharCount.collectAsStateWithLifecycle()

      val isFocusMode by viewModel.isFocusMode.collectAsStateWithLifecycle()
      val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
      val savedPulse by viewModel.savedPulse.collectAsStateWithLifecycle()
      val lastSavedTimestamp by viewModel.lastSavedTimestamp.collectAsStateWithLifecycle()
      val availableFonts by viewModel.availableFonts.collectAsStateWithLifecycle()

      // Determine active theme (per-book override or global theme)
      val effectiveTheme = currentBook?.themeOverride?.let { AppThemeMode.fromString(it) } ?: globalTheme
      val effectiveFont = currentBook?.fontOverride ?: globalFont
      val effectiveFontSize = currentBook?.fontSizeOverride ?: globalFontSize
      val effectiveLineHeight = currentBook?.lineHeightOverride ?: globalLineHeight

      InkwellTheme(themeMode = effectiveTheme) {
        AnimatedContent(
          targetState = when {
            activeChapterId != null -> ScreenState.EDITOR
            selectedBookId != null && currentBook != null -> ScreenState.BOOK_DETAIL
            else -> ScreenState.LIBRARY
          },
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "ScreenTransition",
          modifier = Modifier.fillMaxSize()
        ) { targetScreen ->
          when (targetScreen) {
            ScreenState.LIBRARY -> {
              LibraryScreen(
                books = books,
                lastBackupTime = lastBackupTime,
                backupVersions = backupVersions,
                isBackupRunning = isBackupRunning,
                backupMessage = backupMessage,
                isGoogleConnected = isGoogleConnected,
                googleAccountEmail = googleAccountEmail,
                googleAccountDisplayName = googleAccountDisplayName,
                currentTheme = effectiveTheme,
                currentFontId = effectiveFont,
                currentFontSize = effectiveFontSize,
                currentLineHeight = effectiveLineHeight,
                availableFonts = availableFonts,
                onBookSelected = { viewModel.selectBook(it) },
                onCreateBook = { title, color, label, synopsis ->
                  viewModel.createBook(title, color, label, synopsis)
                },
                onConnectGoogle = {
                  googleSignInLauncher.launch(viewModel.googleAuthHelper.getSignInIntent())
                },
                onDisconnectGoogle = { viewModel.disconnectGoogleAccount() },
                onBackupNow = { viewModel.triggerManualBackup() },
                onRestoreBackup = { viewModel.restoreBackupVersion(it) },
                onThemeSelected = { viewModel.setGlobalTheme(it) },
                onFontSelected = { viewModel.setGlobalFont(it) },
                onFontSizeChanged = { viewModel.setGlobalFontSize(it) },
                onLineHeightChanged = { viewModel.setGlobalLineHeight(it) },
                onImportFont = { viewModel.importCustomFont(it) },
                onDeleteCustomFont = { viewModel.deleteCustomFont(it) },
                onClearBackupMessage = { viewModel.clearBackupMessage() }
              )
            }

            ScreenState.BOOK_DETAIL -> {
              currentBook?.let { book ->
                BookDetailScreen(
                  book = book,
                  chapters = currentBookChapters,
                  totalWordCount = currentBookTotalWords,
                  viewLayoutMode = chapterViewMode,
                  currentTheme = effectiveTheme,
                  currentFontId = effectiveFont,
                  currentFontSize = effectiveFontSize,
                  currentLineHeight = effectiveLineHeight,
                  availableFonts = availableFonts,
                  onBack = { viewModel.selectBook(-1L).also { viewModel.clearActiveChapter() } },
                  onChapterSelected = { viewModel.selectChapter(it) },
                  onCreateChapter = { bookId, customTitle -> viewModel.createChapter(bookId, customTitle) },
                  onDeleteChapter = { viewModel.deleteChapter(it) },
                  onStatusChanged = { chapter, status -> viewModel.selectChapter(chapter.id).also { viewModel.onStatusChanged(status) } },
                  onMoveUp = { viewModel.moveChapterUp(it) },
                  onMoveDown = { viewModel.moveChapterDown(it) },
                  onToggleViewLayout = { viewModel.setChapterViewMode(it) },
                  onUpdateBook = { viewModel.updateBook(it) },
                  onDeleteBook = { viewModel.deleteBook(it) },
                  onThemeSelected = { viewModel.setGlobalTheme(it) },
                  onFontSelected = { viewModel.setGlobalFont(it) },
                  onFontSizeChanged = { viewModel.setGlobalFontSize(it) },
                  onLineHeightChanged = { viewModel.setGlobalLineHeight(it) },
                  onImportFont = { viewModel.importCustomFont(it) },
                  onDeleteCustomFont = { viewModel.deleteCustomFont(it) }
                )
              }
            }

            ScreenState.EDITOR -> {
              EditorScreen(
                book = currentBook,
                chapter = activeChapter,
                bookTotalWords = currentBookTotalWords,
                title = editorTitle,
                content = editorContent,
                status = editorStatus,
                wordCount = chapterWordCount,
                charCount = chapterCharCount,
                isFocusMode = isFocusMode,
                isSaving = isSaving,
                savedPulse = savedPulse,
                lastSavedTimestamp = lastSavedTimestamp,
                currentTheme = effectiveTheme,
                currentFontId = effectiveFont,
                currentFontSize = effectiveFontSize,
                currentLineHeight = effectiveLineHeight,
                typewriterScrollEnabled = typewriterScrollEnabled,
                availableFonts = availableFonts,
                onTitleChanged = { viewModel.onTitleChanged(it) },
                onContentChanged = { viewModel.onContentChanged(it) },
                onStatusChanged = { viewModel.onStatusChanged(it) },
                onToggleFocusMode = { viewModel.toggleFocusMode() },
                onBack = { viewModel.clearActiveChapter() },
                onThemeSelected = { viewModel.setGlobalTheme(it) },
                onFontSelected = { viewModel.setGlobalFont(it) },
                onFontSizeChanged = { viewModel.setGlobalFontSize(it) },
                onLineHeightChanged = { viewModel.setGlobalLineHeight(it) },
                onImportFont = { viewModel.importCustomFont(it) },
                onDeleteCustomFont = { viewModel.deleteCustomFont(it) }
              )
            }
          }
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    viewModel.flushActiveChapterSave()
  }
}

enum class ScreenState {
  LIBRARY,
  BOOK_DETAIL,
  EDITOR
}
