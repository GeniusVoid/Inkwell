package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val coverColor: String = "#8B1E2E",
  val coverLabel: String = "NOVEL",
  val synopsis: String = "",
  val fontOverride: String? = null,
  val fontSizeOverride: Float? = null,
  val lineHeightOverride: Float? = null,
  val themeOverride: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
