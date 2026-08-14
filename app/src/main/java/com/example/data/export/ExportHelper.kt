package com.example.data.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.BookEntity
import com.example.data.local.ChapterEntity

object ExportHelper {

  fun copyToClipboard(context: Context, label: String, text: String): Boolean {
    return try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = ClipData.newPlainText(label, text)
      clipboard.setPrimaryClip(clip)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun shareChapter(context: Context, chapter: ChapterEntity, format: ExportFormat) {
    val title = chapter.title.ifBlank { "Untitled Chapter" }
    val formattedContent = when (format) {
      ExportFormat.MARKDOWN -> "# $title\n\n${chapter.content}"
      ExportFormat.PLAIN_TEXT -> "$title\n\n${chapter.content}"
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, formattedContent)
    }

    val chooser = Intent.createChooser(intent, "Export Chapter ($format)")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
  }

  fun shareFullBook(context: Context, book: BookEntity, chapters: List<ChapterEntity>, format: ExportFormat) {
    val title = book.title.ifBlank { "Untitled Novel" }
    val sb = StringBuilder()

    when (format) {
      ExportFormat.MARKDOWN -> {
        sb.append("# ").append(title).append("\n\n")
        if (book.synopsis.isNotBlank()) {
          sb.append("> ").append(book.synopsis.replace("\n", "\n> ")).append("\n\n---\n\n")
        }
        chapters.forEachIndexed { index, chapter ->
          val chTitle = chapter.title.ifBlank { "Chapter ${index + 1}" }
          sb.append("## ").append(chTitle).append("\n\n")
          sb.append(chapter.content).append("\n\n---\n\n")
        }
      }
      ExportFormat.PLAIN_TEXT -> {
        sb.append(title.uppercase()).append("\n")
        sb.append("=".repeat(title.length.coerceAtLeast(10))).append("\n\n")
        if (book.synopsis.isNotBlank()) {
          sb.append(book.synopsis).append("\n\n--------------------------------\n\n")
        }
        chapters.forEachIndexed { index, chapter ->
          val chTitle = chapter.title.ifBlank { "Chapter ${index + 1}" }
          sb.append(chTitle).append("\n\n")
          sb.append(chapter.content).append("\n\n--------------------------------\n\n")
        }
      }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, sb.toString().trim())
    }

    val chooser = Intent.createChooser(intent, "Export Novel ($format)")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
  }
}

enum class ExportFormat(val extension: String, val label: String) {
  MARKDOWN(".md", "Markdown (.md)"),
  PLAIN_TEXT(".txt", "Plain Text (.txt)")
}
