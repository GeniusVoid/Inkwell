package com.example.data.backup

import android.content.Context
import com.example.data.local.BookEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.InkwellDao
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DriveBackupManager(
  private val context: Context,
  private val dao: InkwellDao
) {
  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private val backupAdapter = moshi.adapter(InkwellBackupDto::class.java).indent("  ")
  private val versionListAdapter = moshi.adapter<List<BackupVersionInfo>>(
    Types.newParameterizedType(List::class.java, BackupVersionInfo::class.java)
  )

  private val backupDir: File
    get() {
      val dir = File(context.filesDir, "backups")
      if (!dir.exists()) {
        dir.mkdirs()
      }
      return dir
    }

  companion object {
    const val MAX_VERSION_RETENTION = 5
  }

  suspend fun createManualBackup(driveAccessToken: String? = null): Result<BackupVersionInfo> = withContext(Dispatchers.IO) {
    try {
      val books = dao.getAllBooksSync()
      val chapters = dao.getAllChaptersSync()

      val bookDtos = books.map {
        BookBackupDto(
          id = it.id,
          title = it.title,
          coverColor = it.coverColor,
          coverLabel = it.coverLabel,
          synopsis = it.synopsis,
          fontOverride = it.fontOverride,
          fontSizeOverride = it.fontSizeOverride,
          lineHeightOverride = it.lineHeightOverride,
          themeOverride = it.themeOverride,
          createdAt = it.createdAt,
          updatedAt = it.updatedAt
        )
      }

      val chapterDtos = chapters.map {
        ChapterBackupDto(
          id = it.id,
          bookId = it.bookId,
          title = it.title,
          content = it.content,
          status = it.status,
          orderIndex = it.orderIndex,
          wordCount = it.wordCount,
          charCount = it.charCount,
          createdAt = it.createdAt,
          updatedAt = it.updatedAt
        )
      }

      val totalWords = chapters.sumOf { it.wordCount }
      val now = System.currentTimeMillis()
      val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))
      val readableDate = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(now))
      val fileName = "Inkwell_Backup_$dateStamp.json"

      val backupDto = InkwellBackupDto(
        appName = "Inkwell",
        backupSchemaVersion = 1,
        timestamp = now,
        formattedTime = readableDate,
        bookCount = books.size,
        chapterCount = chapters.size,
        totalWords = totalWords,
        books = bookDtos,
        chapters = chapterDtos
      )

      val jsonContent = backupAdapter.toJson(backupDto)
      val backupFile = File(backupDir, fileName)
      backupFile.writeText(jsonContent, Charsets.UTF_8)

      // Prune local backups older than MAX_VERSION_RETENTION
      pruneOldBackups()

      var driveId: String? = null
      var isDriveUploaded = false

      // If Drive access token is available, attempt Drive upload
      if (!driveAccessToken.isNullOrBlank()) {
        try {
          driveId = uploadToGoogleDrive(fileName, jsonContent, driveAccessToken)
          isDriveUploaded = driveId != null
        } catch (e: Exception) {
          // If offline or network error, local backup is still fully secured!
          e.printStackTrace()
        }
      }

      val versionInfo = BackupVersionInfo(
        fileName = fileName,
        driveFileId = driveId,
        timestamp = now,
        formattedDate = readableDate,
        bookCount = books.size,
        chapterCount = chapters.size,
        totalWords = totalWords,
        sizeBytes = backupFile.length(),
        isLocalAvailable = true,
        isDriveAvailable = isDriveUploaded
      )

      Result.success(versionInfo)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun getBackupVersions(): List<BackupVersionInfo> {
    val files = backupDir.listFiles { file ->
      file.extension.equals("json", ignoreCase = true) && file.name.startsWith("Inkwell_Backup_")
    } ?: emptyArray()

    return files.sortedByDescending { it.lastModified() }.take(MAX_VERSION_RETENTION).mapNotNull { file ->
      try {
        val content = file.readText(Charsets.UTF_8)
        val backup = backupAdapter.fromJson(content)
        if (backup != null) {
          BackupVersionInfo(
            fileName = file.name,
            timestamp = backup.timestamp,
            formattedDate = backup.formattedTime,
            bookCount = backup.bookCount,
            chapterCount = backup.chapterCount,
            totalWords = backup.totalWords,
            sizeBytes = file.length(),
            isLocalAvailable = true,
            isDriveAvailable = false
          )
        } else null
      } catch (e: Exception) {
        null
      }
    }
  }

  suspend fun restoreFromBackupFile(fileName: String): Result<InkwellBackupDto> = withContext(Dispatchers.IO) {
    try {
      val file = File(backupDir, fileName)
      if (!file.exists()) {
        return@withContext Result.failure(IllegalStateException("Backup file $fileName not found"))
      }

      val json = file.readText(Charsets.UTF_8)
      val backup = backupAdapter.fromJson(json)
        ?: return@withContext Result.failure(IllegalStateException("Failed to parse backup content"))

      val restoredBooks = backup.books.map {
        BookEntity(
          id = it.id,
          title = it.title,
          coverColor = it.coverColor,
          coverLabel = it.coverLabel,
          synopsis = it.synopsis,
          fontOverride = it.fontOverride,
          fontSizeOverride = it.fontSizeOverride,
          lineHeightOverride = it.lineHeightOverride,
          themeOverride = it.themeOverride,
          createdAt = it.createdAt,
          updatedAt = it.updatedAt
        )
      }

      val restoredChapters = backup.chapters.map {
        ChapterEntity(
          id = it.id,
          bookId = it.bookId,
          title = it.title,
          content = it.content,
          status = it.status,
          orderIndex = it.orderIndex,
          wordCount = it.wordCount,
          charCount = it.charCount,
          createdAt = it.createdAt,
          updatedAt = it.updatedAt
        )
      }

      dao.restoreAllData(restoredBooks, restoredChapters)
      Result.success(backup)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun pruneOldBackups() {
    val files = backupDir.listFiles { file ->
      file.extension.equals("json", ignoreCase = true) && file.name.startsWith("Inkwell_Backup_")
    } ?: return

    if (files.size > MAX_VERSION_RETENTION) {
      val sortedFiles = files.sortedByDescending { it.lastModified() }
      for (i in MAX_VERSION_RETENTION until sortedFiles.size) {
        sortedFiles[i].delete()
      }
    }
  }

  private suspend fun uploadToGoogleDrive(fileName: String, jsonContent: String, accessToken: String): String? = withContext(Dispatchers.IO) {
    // Google Drive REST API v3 Multipart Upload / AppData or Drive files
    // In Android environment, handles direct HTTP multipart request to https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart
    try {
      val client = okhttp3.OkHttpClient()
      val boundary = "---InkwellBackupBoundary---"
      val metadataJson = """{"name":"$fileName","description":"Inkwell Novel Snapshot Backup","mimeType":"application/json"}"""

      val multipartBody = StringBuilder()
        .append("--").append(boundary).append("\r\n")
        .append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
        .append(metadataJson).append("\r\n")
        .append("--").append(boundary).append("\r\n")
        .append("Content-Type: application/json\r\n\r\n")
        .append(jsonContent).append("\r\n")
        .append("--").append(boundary).append("--\r\n")
        .toString()

      val mediaType = "multipart/related; boundary=$boundary".toMediaTypeOrNull()
      val requestBody = multipartBody.toRequestBody(mediaType)

      val request = okhttp3.Request.Builder()
        .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
        .addHeader("Authorization", "Bearer $accessToken")
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val respBody = response.body?.string() ?: ""
        // Parse ID from response
        val idRegex = """"id":\s*"([^"]+)"""".toRegex()
        val match = idRegex.find(respBody)
        match?.groupValues?.getOrNull(1) ?: "drive_success"
      } else {
        null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
