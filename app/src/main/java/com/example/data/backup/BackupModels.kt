package com.example.data.backup

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BookBackupDto(
  val id: Long,
  val title: String,
  val coverColor: String,
  val coverLabel: String,
  val synopsis: String,
  val fontOverride: String?,
  val fontSizeOverride: Float?,
  val lineHeightOverride: Float?,
  val themeOverride: String?,
  val createdAt: Long,
  val updatedAt: Long
)

data class ChapterBackupDto(
  val id: Long,
  val bookId: Long,
  val title: String,
  val content: String,
  val status: String,
  val orderIndex: Int,
  val wordCount: Int,
  val charCount: Int,
  val createdAt: Long,
  val updatedAt: Long
)

data class InkwellBackupDto(
  val appName: String = "Inkwell",
  val backupSchemaVersion: Int = 1,
  val timestamp: Long = System.currentTimeMillis(),
  val formattedTime: String = SimpleDateFormat("MMM dd, yyyy · HH:mm:ss", Locale.getDefault()).format(Date()),
  val bookCount: Int,
  val chapterCount: Int,
  val totalWords: Int,
  val books: List<BookBackupDto>,
  val chapters: List<ChapterBackupDto>
)

data class BackupVersionInfo(
  val fileName: String,
  val driveFileId: String? = null,
  val timestamp: Long,
  val formattedDate: String,
  val bookCount: Int,
  val chapterCount: Int,
  val totalWords: Int,
  val sizeBytes: Long = 0L,
  val isLocalAvailable: Boolean = true,
  val isDriveAvailable: Boolean = false
)
