package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "chapters",
  foreignKeys = [
    ForeignKey(
      entity = BookEntity::class,
      parentColumns = ["id"],
      childColumns = ["bookId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["bookId"])]
)
data class ChapterEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val bookId: Long,
  val title: String,
  val content: String = "",
  val status: String = "DRAFT", // DRAFT, EDITING, DONE
  val orderIndex: Int = 0,
  val wordCount: Int = 0,
  val charCount: Int = 0,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
