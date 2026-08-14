package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeMode
import com.example.data.model.ViewLayoutMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "inkwell_settings")

class AppPreferences(private val context: Context) {

  private object Keys {
    val GLOBAL_THEME = stringPreferencesKey("global_theme")
    val GLOBAL_FONT = stringPreferencesKey("global_font")
    val GLOBAL_FONT_SIZE = floatPreferencesKey("global_font_size")
    val GLOBAL_LINE_HEIGHT = floatPreferencesKey("global_line_height")
    val CHAPTER_VIEW_MODE = stringPreferencesKey("chapter_view_mode")
    val TYPEWRITER_SCROLL = booleanPreferencesKey("typewriter_scroll")
    val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
    val LAST_BACKUP_STATUS = stringPreferencesKey("last_backup_status")
    val BACKUP_VERSIONS_JSON = stringPreferencesKey("backup_versions_json")
  }

  val globalThemeFlow: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
    val themeName = prefs[Keys.GLOBAL_THEME] ?: AppThemeMode.PAPER_SEPIA.name
    AppThemeMode.fromString(themeName)
  }

  val globalFontFlow: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[Keys.GLOBAL_FONT] ?: "preset_serif_literary"
  }

  val globalFontSizeFlow: Flow<Float> = context.dataStore.data.map { prefs ->
    prefs[Keys.GLOBAL_FONT_SIZE] ?: 18.5f
  }

  val globalLineHeightFlow: Flow<Float> = context.dataStore.data.map { prefs ->
    prefs[Keys.GLOBAL_LINE_HEIGHT] ?: 1.65f
  }

  val chapterViewModeFlow: Flow<ViewLayoutMode> = context.dataStore.data.map { prefs ->
    val modeStr = prefs[Keys.CHAPTER_VIEW_MODE] ?: ViewLayoutMode.GRID.name
    try {
      ViewLayoutMode.valueOf(modeStr)
    } catch (e: Exception) {
      ViewLayoutMode.GRID
    }
  }

  val typewriterScrollFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[Keys.TYPEWRITER_SCROLL] ?: true
  }

  val lastBackupTimeFlow: Flow<Long> = context.dataStore.data.map { prefs ->
    prefs[Keys.LAST_BACKUP_TIME] ?: 0L
  }

  val lastBackupStatusFlow: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[Keys.LAST_BACKUP_STATUS] ?: "No backups created yet"
  }

  val backupVersionsJsonFlow: Flow<String> = context.dataStore.data.map { prefs ->
    prefs[Keys.BACKUP_VERSIONS_JSON] ?: "[]"
  }

  suspend fun setGlobalTheme(theme: AppThemeMode) {
    context.dataStore.edit { prefs ->
      prefs[Keys.GLOBAL_THEME] = theme.name
    }
  }

  suspend fun setGlobalFont(fontId: String) {
    context.dataStore.edit { prefs ->
      prefs[Keys.GLOBAL_FONT] = fontId
    }
  }

  suspend fun setGlobalFontSize(sizeSp: Float) {
    context.dataStore.edit { prefs ->
      prefs[Keys.GLOBAL_FONT_SIZE] = sizeSp
    }
  }

  suspend fun setGlobalLineHeight(multiplier: Float) {
    context.dataStore.edit { prefs ->
      prefs[Keys.GLOBAL_LINE_HEIGHT] = multiplier
    }
  }

  suspend fun setChapterViewMode(mode: ViewLayoutMode) {
    context.dataStore.edit { prefs ->
      prefs[Keys.CHAPTER_VIEW_MODE] = mode.name
    }
  }

  suspend fun setTypewriterScroll(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.TYPEWRITER_SCROLL] = enabled
    }
  }

  suspend fun recordBackupSuccess(timestamp: Long, versionInfoJson: String, summaryText: String) {
    context.dataStore.edit { prefs ->
      prefs[Keys.LAST_BACKUP_TIME] = timestamp
      prefs[Keys.LAST_BACKUP_STATUS] = summaryText
      prefs[Keys.BACKUP_VERSIONS_JSON] = versionInfoJson
    }
  }
}
