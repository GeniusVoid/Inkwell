package com.example.data.font

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import java.io.File
import java.io.FileOutputStream

data class FontItem(
  val id: String,
  val name: String,
  val category: String, // "Pre-installed Serif", "Pre-installed Sans", "Pre-installed Mono", "Custom"
  val isCustom: Boolean = false,
  val filePath: String? = null
)

object FontRegistry {
  val PRESET_FONTS = listOf(
    FontItem(
      id = "preset_serif_literary",
      name = "Literary Serif",
      category = "Serif (Reading)"
    ),
    FontItem(
      id = "preset_serif_classic",
      name = "Book Antiqua Serif",
      category = "Serif (Reading)"
    ),
    FontItem(
      id = "preset_sans_modern",
      name = "Modern Sans",
      category = "Sans-Serif (Clean)"
    ),
    FontItem(
      id = "preset_sans_editorial",
      name = "Editorial Sans",
      category = "Sans-Serif (Clean)"
    ),
    FontItem(
      id = "preset_mono_typewriter",
      name = "Drafting Monospace",
      category = "Monospace (Drafting)"
    )
  )

  fun resolveFontFamily(context: Context, fontId: String?): FontFamily {
    if (fontId.isNullOrEmpty()) return FontFamily.Serif

    val preset = PRESET_FONTS.find { it.id == fontId }
    if (preset != null) {
      return when (preset.id) {
        "preset_serif_literary" -> FontFamily.Serif
        "preset_serif_classic" -> FontFamily.Serif
        "preset_sans_modern" -> FontFamily.SansSerif
        "preset_sans_editorial" -> FontFamily.Default
        "preset_mono_typewriter" -> FontFamily.Monospace
        else -> FontFamily.Serif
      }
    }

    // Check custom font
    val customFontFile = File(context.filesDir, "fonts/$fontId")
    if (customFontFile.exists() && customFontFile.isFile) {
      return try {
        FontFamily(
          Font(
            file = customFontFile,
            weight = FontWeight.Normal,
            style = FontStyle.Normal
          )
        )
      } catch (e: Exception) {
        FontFamily.Serif
      }
    }

    return FontFamily.Serif
  }
}

class CustomFontManager(private val context: Context) {
  private val fontsDir: File
    get() {
      val dir = File(context.filesDir, "fonts")
      if (!dir.exists()) {
        dir.mkdirs()
      }
      return dir
    }

  fun getCustomFonts(): List<FontItem> {
    val dir = fontsDir
    val files = dir.listFiles { file ->
      file.extension.equals("ttf", ignoreCase = true) || file.extension.equals("otf", ignoreCase = true)
    } ?: emptyArray()

    return files.map { file ->
      val displayName = file.nameWithoutExtension.replace('_', ' ').replace('-', ' ').replaceFirstChar { it.uppercase() }
      FontItem(
        id = file.name,
        name = displayName,
        category = "Custom Font",
        isCustom = true,
        filePath = file.absolutePath
      )
    }
  }

  fun getAllAvailableFonts(): List<FontItem> {
    return FontRegistry.PRESET_FONTS + getCustomFonts()
  }

  fun importFont(uri: Uri): Result<FontItem> {
    return try {
      val contentResolver = context.contentResolver
      var fileName = "custom_font_${System.currentTimeMillis()}.ttf"

      contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            val queriedName = cursor.getString(nameIndex)
            if (!queriedName.isNullOrBlank()) {
              fileName = queriedName.replace(" ", "_")
            }
          }
        }
      }

      val destinationFile = File(fontsDir, fileName)
      contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(destinationFile).use { outputStream ->
          inputStream.copyTo(outputStream)
        }
      }

      val displayName = destinationFile.nameWithoutExtension.replace('_', ' ').replace('-', ' ').replaceFirstChar { it.uppercase() }
      Result.success(
        FontItem(
          id = destinationFile.name,
          name = displayName,
          category = "Custom Font",
          isCustom = true,
          filePath = destinationFile.absolutePath
        )
      )
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun deleteCustomFont(fontId: String): Boolean {
    val file = File(fontsDir, fontId)
    return if (file.exists()) file.delete() else false
  }
}
