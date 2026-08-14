package com.example.data.model

enum class ChapterStatus(val displayName: String, val code: String) {
  DRAFT("Draft", "DRAFT"),
  EDITING("Editing", "EDITING"),
  DONE("Done", "DONE");

  companion object {
    fun fromCode(code: String): ChapterStatus {
      return entries.find { it.code.equals(code, ignoreCase = true) } ?: DRAFT
    }
  }
}

enum class AppThemeMode(val displayName: String, val description: String) {
  PAPER_SEPIA("Paper Sepia", "Warm linen & amber"),
  SOFT_DARK("Soft Dark", "Café at night charcoal"),
  TRUE_DARK("True Dark", "Deep OLED black"),
  IVORY_LIGHT("Ivory Light", "Crisp editorial white");

  companion object {
    fun fromString(name: String?): AppThemeMode {
      return entries.find { it.name.equals(name, ignoreCase = true) } ?: PAPER_SEPIA
    }
  }
}

enum class ViewLayoutMode {
  GRID,
  LIST
}

data class CoverPalette(
  val id: String,
  val name: String,
  val hexColor: String,
  val textColorHex: String = "#FFFFFF"
)

val PRESET_COVER_PALETTES = listOf(
  CoverPalette("crimson", "Crimson Velvet", "#8B1E2E"),
  CoverPalette("forest", "Forest Moss", "#244A38"),
  CoverPalette("midnight", "Midnight Ink", "#1B2A4A"),
  CoverPalette("amber", "Vintage Amber", "#9E641E"),
  CoverPalette("amethyst", "Royal Amethyst", "#4A225D"),
  CoverPalette("espresso", "Espresso Leather", "#3E2A20"),
  CoverPalette("slate", "Warm Slate", "#38414E"),
  CoverPalette("terracotta", "Terracotta Clay", "#8C3B2B")
)
