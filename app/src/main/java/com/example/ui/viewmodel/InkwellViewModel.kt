package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupVersionInfo
import com.example.data.backup.DriveBackupManager
import com.example.data.backup.GoogleDriveAuthHelper
import com.example.data.font.CustomFontManager
import com.example.data.font.FontItem
import com.example.data.local.AppPreferences
import com.example.data.local.BookEntity
import com.example.data.local.BookWithStats
import com.example.data.local.ChapterEntity
import com.example.data.local.InkwellDatabase
import com.example.data.model.AppThemeMode
import com.example.data.model.ChapterStatus
import com.example.data.model.ViewLayoutMode
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InkwellViewModel(application: Application) : AndroidViewModel(application) {

  private val database = InkwellDatabase.getDatabase(application, viewModelScope)
  private val dao = database.inkwellDao()
  private val preferences = AppPreferences(application)
  val fontManager = CustomFontManager(application)
  private val backupManager = DriveBackupManager(application, dao)

  // Preferences Flows
  val globalTheme: StateFlow<AppThemeMode> = preferences.globalThemeFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.PAPER_SEPIA)

  val globalFont: StateFlow<String> = preferences.globalFontFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "preset_serif_literary")

  val globalFontSize: StateFlow<Float> = preferences.globalFontSizeFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18.5f)

  val globalLineHeight: StateFlow<Float> = preferences.globalLineHeightFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.65f)

  val chapterViewMode: StateFlow<ViewLayoutMode> = preferences.chapterViewModeFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ViewLayoutMode.GRID)

  val typewriterScrollEnabled: StateFlow<Boolean> = preferences.typewriterScrollFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  val lastBackupTime: StateFlow<Long> = preferences.lastBackupTimeFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

  val lastBackupStatus: StateFlow<String> = preferences.lastBackupStatusFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "No backups created yet")

  // Books with live stats
  val booksWithStats: StateFlow<List<BookWithStats>> = dao.getAllBooksWithStats()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Selected Book & Chapters
  private val _selectedBookId = MutableStateFlow<Long?>(null)
  val selectedBookId: StateFlow<Long?> = _selectedBookId.asStateFlow()

  val currentBook: StateFlow<BookEntity?> = _selectedBookId.flatMapLatest { id ->
    if (id != null) dao.getBookById(id) else flowOf(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val currentBookChapters: StateFlow<List<ChapterEntity>> = _selectedBookId.flatMapLatest { id ->
    if (id != null) dao.getChaptersForBook(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val currentBookTotalWords: StateFlow<Int> = _selectedBookId.flatMapLatest { id ->
    if (id != null) dao.getBookTotalWordCount(id) else flowOf(0)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // Active Chapter for Editor
  private val _activeChapterId = MutableStateFlow<Long?>(null)
  val activeChapterId: StateFlow<Long?> = _activeChapterId.asStateFlow()

  val activeChapter: StateFlow<ChapterEntity?> = _activeChapterId.flatMapLatest { id ->
    if (id != null) dao.getChapterById(id) else flowOf(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Editor In-Memory Live Text & State
  private val _editorTitle = MutableStateFlow("")
  val editorTitle: StateFlow<String> = _editorTitle.asStateFlow()

  private val _editorContent = MutableStateFlow("")
  val editorContent: StateFlow<String> = _editorContent.asStateFlow()

  private val _editorStatus = MutableStateFlow(ChapterStatus.DRAFT)
  val editorStatus: StateFlow<ChapterStatus> = _editorStatus.asStateFlow()

  // Live Counts
  private val _chapterWordCount = MutableStateFlow(0)
  val chapterWordCount: StateFlow<Int> = _chapterWordCount.asStateFlow()

  private val _chapterCharCount = MutableStateFlow(0)
  val chapterCharCount: StateFlow<Int> = _chapterCharCount.asStateFlow()

  // Focus Mode
  private val _isFocusMode = MutableStateFlow(false)
  val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

  // Autosave Status Indicators
  private val _isSaving = MutableStateFlow(false)
  val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

  private val _savedPulse = MutableStateFlow(false)
  val savedPulse: StateFlow<Boolean> = _savedPulse.asStateFlow()

  private val _lastSavedTimestamp = MutableStateFlow<Long>(System.currentTimeMillis())
  val lastSavedTimestamp: StateFlow<Long> = _lastSavedTimestamp.asStateFlow()

  // Available Fonts List
  private val _availableFonts = MutableStateFlow<List<FontItem>>(emptyList())
  val availableFonts: StateFlow<List<FontItem>> = _availableFonts.asStateFlow()

  // Backup Versions List
  private val _backupVersions = MutableStateFlow<List<BackupVersionInfo>>(emptyList())
  val backupVersions: StateFlow<List<BackupVersionInfo>> = _backupVersions.asStateFlow()

  private val _isBackupRunning = MutableStateFlow(false)
  val isBackupRunning: StateFlow<Boolean> = _isBackupRunning.asStateFlow()

  private val _backupMessage = MutableStateFlow<String?>(null)
  val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

  // Google Drive Auth Helper & Account State
  val googleAuthHelper = GoogleDriveAuthHelper(application)

  private val _googleAccountEmail = MutableStateFlow<String?>(null)
  val googleAccountEmail: StateFlow<String?> = _googleAccountEmail.asStateFlow()

  private val _googleAccountDisplayName = MutableStateFlow<String?>(null)
  val googleAccountDisplayName: StateFlow<String?> = _googleAccountDisplayName.asStateFlow()

  private val _isGoogleConnected = MutableStateFlow(false)
  val isGoogleConnected: StateFlow<Boolean> = _isGoogleConnected.asStateFlow()

  private var autosaveDebounceJob: Job? = null

  init {
    refreshFontsList()
    refreshBackupVersions()
    checkExistingGoogleSignIn()
  }

  private fun checkExistingGoogleSignIn() {
    val account = googleAuthHelper.getSignedInAccount()
    if (account != null) {
      _googleAccountEmail.value = account.email
      _googleAccountDisplayName.value = account.displayName
      _isGoogleConnected.value = true
    }
  }

  fun updateGoogleAccount(account: GoogleSignInAccount?) {
    if (account != null) {
      _googleAccountEmail.value = account.email
      _googleAccountDisplayName.value = account.displayName
      _isGoogleConnected.value = true
      _backupMessage.value = "Google Drive connected (${account.email})"
    } else {
      _googleAccountEmail.value = null
      _googleAccountDisplayName.value = null
      _isGoogleConnected.value = false
    }
  }

  fun onGoogleSignInFailed(error: String) {
    _backupMessage.value = "Google sign-in: $error"
  }

  fun disconnectGoogleAccount() {
    googleAuthHelper.signOut {
      _googleAccountEmail.value = null
      _googleAccountDisplayName.value = null
      _isGoogleConnected.value = false
      _backupMessage.value = "Google Drive disconnected"
    }
  }

  fun refreshFontsList() {
    _availableFonts.value = fontManager.getAllAvailableFonts()
  }

  fun refreshBackupVersions() {
    viewModelScope.launch {
      _backupVersions.value = backupManager.getBackupVersions()
    }
  }

  fun selectBook(bookId: Long) {
    _selectedBookId.value = bookId
  }

  fun selectChapter(chapterId: Long) {
    _activeChapterId.value = chapterId
    viewModelScope.launch {
      val chapter = dao.getChapterByIdSync(chapterId)
      if (chapter != null) {
        _editorTitle.value = chapter.title
        _editorContent.value = chapter.content
        _editorStatus.value = ChapterStatus.fromCode(chapter.status)
        updateCounts(chapter.content)
      }
    }
  }

  fun clearActiveChapter() {
    // Force final save before exiting
    flushActiveChapterSave()
    _activeChapterId.value = null
    _isFocusMode.value = false
  }

  fun onContentChanged(newContent: String) {
    _editorContent.value = newContent
    updateCounts(newContent)
    scheduleAutosave()
  }

  fun onTitleChanged(newTitle: String) {
    _editorTitle.value = newTitle
    scheduleAutosave()
  }

  fun onStatusChanged(newStatus: ChapterStatus) {
    _editorStatus.value = newStatus
    val chId = _activeChapterId.value ?: return
    viewModelScope.launch {
      val now = System.currentTimeMillis()
      dao.updateChapterStatus(chId, newStatus.code, now)
      triggerSavedPulse(now)
    }
  }

  fun toggleFocusMode() {
    _isFocusMode.value = !_isFocusMode.value
  }

  fun setFocusMode(enabled: Boolean) {
    _isFocusMode.value = enabled
  }

  private fun updateCounts(text: String) {
    val trimmed = text.trim()
    val words = if (trimmed.isEmpty()) 0 else trimmed.split("\\s+".toRegex()).size
    val chars = text.length
    _chapterWordCount.value = words
    _chapterCharCount.value = chars
  }

  private fun scheduleAutosave() {
    autosaveDebounceJob?.cancel()
    autosaveDebounceJob = viewModelScope.launch {
      delay(600) // 600ms debounce
      saveCurrentEditorContent()
    }
  }

  fun flushActiveChapterSave() {
    autosaveDebounceJob?.cancel()
    viewModelScope.launch {
      saveCurrentEditorContent()
    }
  }

  private suspend fun saveCurrentEditorContent() {
    val chId = _activeChapterId.value ?: return
    val title = _editorTitle.value
    val content = _editorContent.value
    val words = _chapterWordCount.value
    val chars = _chapterCharCount.value
    val now = System.currentTimeMillis()

    _isSaving.value = true
    try {
      dao.updateChapterTitle(chId, title, now)
      dao.autosaveChapterContent(chId, content, words, chars, now)
      _lastSavedTimestamp.value = now
      triggerSavedPulse(now)
    } finally {
      _isSaving.value = false
    }
  }

  private fun triggerSavedPulse(timestamp: Long) {
    _lastSavedTimestamp.value = timestamp
    viewModelScope.launch {
      _savedPulse.value = true
      delay(1800)
      _savedPulse.value = false
    }
  }

  // Chapter Management
  fun createChapter(bookId: Long, customTitle: String? = null) {
    viewModelScope.launch {
      val maxOrder = dao.getMaxOrderIndex(bookId)
      val chapterNum = maxOrder + 2
      val title = customTitle ?: "Chapter $chapterNum"
      val newChapter = ChapterEntity(
        bookId = bookId,
        title = title,
        content = "",
        status = "DRAFT",
        orderIndex = maxOrder + 1,
        wordCount = 0,
        charCount = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      val newId = dao.insertChapter(newChapter)
      selectChapter(newId)
    }
  }

  fun deleteChapter(chapterId: Long) {
    viewModelScope.launch {
      dao.deleteChapterById(chapterId)
      if (_activeChapterId.value == chapterId) {
        _activeChapterId.value = null
      }
    }
  }

  fun moveChapterUp(index: Int) {
    val list = currentBookChapters.value.toMutableList()
    if (index > 0 && index < list.size) {
      val item = list.removeAt(index)
      list.add(index - 1, item)
      viewModelScope.launch {
        dao.updateChapterOrders(list)
      }
    }
  }

  fun moveChapterDown(index: Int) {
    val list = currentBookChapters.value.toMutableList()
    if (index >= 0 && index < list.size - 1) {
      val item = list.removeAt(index)
      list.add(index + 1, item)
      viewModelScope.launch {
        dao.updateChapterOrders(list)
      }
    }
  }

  fun reorderChapters(fromIndex: Int, toIndex: Int) {
    val list = currentBookChapters.value.toMutableList()
    if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
      val item = list.removeAt(fromIndex)
      list.add(toIndex, item)
      viewModelScope.launch {
        dao.updateChapterOrders(list)
      }
    }
  }

  // Book Management
  fun createBook(title: String, coverColor: String, coverLabel: String, synopsis: String) {
    viewModelScope.launch {
      val newBook = BookEntity(
        title = title.ifBlank { "Untitled Project" },
        coverColor = coverColor,
        coverLabel = coverLabel.ifBlank { "NOVEL" },
        synopsis = synopsis,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      val newId = dao.insertBook(newBook)
      // Automatically add Chapter 1
      dao.insertChapter(
        ChapterEntity(
          bookId = newId,
          title = "Chapter 1",
          content = "",
          status = "DRAFT",
          orderIndex = 0,
          wordCount = 0,
          charCount = 0,
          createdAt = System.currentTimeMillis(),
          updatedAt = System.currentTimeMillis()
        )
      )
      _selectedBookId.value = newId
    }
  }

  fun updateBook(book: BookEntity) {
    viewModelScope.launch {
      dao.updateBook(book.copy(updatedAt = System.currentTimeMillis()))
    }
  }

  fun deleteBook(bookId: Long) {
    viewModelScope.launch {
      dao.deleteBookById(bookId)
      if (_selectedBookId.value == bookId) {
        _selectedBookId.value = null
      }
    }
  }

  // Preferences & Overrides
  fun setGlobalTheme(theme: AppThemeMode) {
    viewModelScope.launch {
      preferences.setGlobalTheme(theme)
    }
  }

  fun setGlobalFont(fontId: String) {
    viewModelScope.launch {
      preferences.setGlobalFont(fontId)
    }
  }

  fun setGlobalFontSize(size: Float) {
    viewModelScope.launch {
      preferences.setGlobalFontSize(size)
    }
  }

  fun setGlobalLineHeight(multiplier: Float) {
    viewModelScope.launch {
      preferences.setGlobalLineHeight(multiplier)
    }
  }

  fun setChapterViewMode(mode: ViewLayoutMode) {
    viewModelScope.launch {
      preferences.setChapterViewMode(mode)
    }
  }

  fun setTypewriterScroll(enabled: Boolean) {
    viewModelScope.launch {
      preferences.setTypewriterScroll(enabled)
    }
  }

  fun importCustomFont(uri: Uri) {
    viewModelScope.launch {
      val result = fontManager.importFont(uri)
      result.onSuccess { fontItem ->
        refreshFontsList()
        preferences.setGlobalFont(fontItem.id)
        _backupMessage.value = "Imported font: ${fontItem.name}"
      }.onFailure {
        _backupMessage.value = "Failed to import font: ${it.localizedMessage}"
      }
    }
  }

  fun deleteCustomFont(fontId: String) {
    viewModelScope.launch {
      fontManager.deleteCustomFont(fontId)
      refreshFontsList()
      if (globalFont.value == fontId) {
        preferences.setGlobalFont("preset_serif_literary")
      }
    }
  }

  // Backup Engine (Manual Only)
  fun triggerManualBackup() {
    viewModelScope.launch {
      _isBackupRunning.value = true
      _backupMessage.value = "Creating snapshot backup..."
      
      var token: String? = null
      val account = googleAuthHelper.getSignedInAccount()
      if (account != null) {
        _backupMessage.value = "Uploading snapshot to Google Drive..."
        token = googleAuthHelper.fetchAccessToken(account)
      }

      val result = backupManager.createManualBackup(token)
      result.onSuccess { versionInfo ->
        val now = System.currentTimeMillis()
        val timeStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(now))
        val driveInfo = if (versionInfo.isDriveAvailable) " (Synced to Google Drive)" else if (account != null) " (Saved locally)" else ""
        val summary = "Last backup: $timeStr (${versionInfo.bookCount} books, ${versionInfo.totalWords} words)$driveInfo"
        preferences.recordBackupSuccess(now, versionInfo.driveFileId ?: "", summary)
        refreshBackupVersions()
        _backupMessage.value = "Backup created successfully! $timeStr$driveInfo"
      }.onFailure {
        _backupMessage.value = "Backup failed: ${it.localizedMessage}"
      }
      _isBackupRunning.value = false
    }
  }

  fun restoreBackupVersion(fileName: String) {
    viewModelScope.launch {
      _isBackupRunning.value = true
      _backupMessage.value = "Restoring backup..."
      val result = backupManager.restoreFromBackupFile(fileName)
      result.onSuccess { backup ->
        _backupMessage.value = "Restored ${backup.bookCount} books and ${backup.chapterCount} chapters!"
        _selectedBookId.value = null
        _activeChapterId.value = null
        refreshBackupVersions()
      }.onFailure {
        _backupMessage.value = "Restore failed: ${it.localizedMessage}"
      }
      _isBackupRunning.value = false
    }
  }

  fun clearBackupMessage() {
    _backupMessage.value = null
  }
}
