package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class BookWithStats(
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
  val updatedAt: Long,
  val chapterCount: Int,
  val totalWordCount: Int
)

@Dao
interface InkwellDao {

  @Query("""
    SELECT 
      b.id,
      b.title,
      b.coverColor,
      b.coverLabel,
      b.synopsis,
      b.fontOverride,
      b.fontSizeOverride,
      b.lineHeightOverride,
      b.themeOverride,
      b.createdAt,
      b.updatedAt,
      COUNT(c.id) AS chapterCount,
      COALESCE(SUM(c.wordCount), 0) AS totalWordCount
    FROM books b
    LEFT JOIN chapters c ON b.id = c.bookId
    GROUP BY b.id
    ORDER BY b.updatedAt DESC
  """)
  fun getAllBooksWithStats(): Flow<List<BookWithStats>>

  @Query("SELECT * FROM books WHERE id = :bookId")
  fun getBookById(bookId: Long): Flow<BookEntity?>

  @Query("SELECT * FROM books WHERE id = :bookId")
  suspend fun getBookByIdSync(bookId: Long): BookEntity?

  @Query("SELECT * FROM books")
  suspend fun getAllBooksSync(): List<BookEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBook(book: BookEntity): Long

  @Update
  suspend fun updateBook(book: BookEntity)

  @Delete
  suspend fun deleteBook(book: BookEntity)

  @Query("DELETE FROM books WHERE id = :bookId")
  suspend fun deleteBookById(bookId: Long)

  // Chapters
  @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY orderIndex ASC, id ASC")
  fun getChaptersForBook(bookId: Long): Flow<List<ChapterEntity>>

  @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY orderIndex ASC, id ASC")
  suspend fun getChaptersForBookSync(bookId: Long): List<ChapterEntity>

  @Query("SELECT * FROM chapters")
  suspend fun getAllChaptersSync(): List<ChapterEntity>

  @Query("SELECT * FROM chapters WHERE id = :chapterId")
  fun getChapterById(chapterId: Long): Flow<ChapterEntity?>

  @Query("SELECT * FROM chapters WHERE id = :chapterId")
  suspend fun getChapterByIdSync(chapterId: Long): ChapterEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChapter(chapter: ChapterEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChapters(chapters: List<ChapterEntity>)

  @Update
  suspend fun updateChapter(chapter: ChapterEntity)

  @Query("""
    UPDATE chapters 
    SET content = :content, 
        wordCount = :wordCount, 
        charCount = :charCount, 
        updatedAt = :updatedAt 
    WHERE id = :chapterId
  """)
  suspend fun autosaveChapterContent(
    chapterId: Long,
    content: String,
    wordCount: Int,
    charCount: Int,
    updatedAt: Long
  )

  @Query("UPDATE chapters SET status = :status, updatedAt = :updatedAt WHERE id = :chapterId")
  suspend fun updateChapterStatus(chapterId: Long, status: String, updatedAt: Long)

  @Query("UPDATE chapters SET title = :title, updatedAt = :updatedAt WHERE id = :chapterId")
  suspend fun updateChapterTitle(chapterId: Long, title: String, updatedAt: Long)

  @Delete
  suspend fun deleteChapter(chapter: ChapterEntity)

  @Query("DELETE FROM chapters WHERE id = :chapterId")
  suspend fun deleteChapterById(chapterId: Long)

  @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM chapters WHERE bookId = :bookId")
  suspend fun getMaxOrderIndex(bookId: Long): Int

  @Query("SELECT COALESCE(SUM(wordCount), 0) FROM chapters WHERE bookId = :bookId")
  fun getBookTotalWordCount(bookId: Long): Flow<Int>

  @Transaction
  suspend fun updateChapterOrders(chapters: List<ChapterEntity>) {
    chapters.forEachIndexed { index, chapter ->
      val updated = chapter.copy(orderIndex = index)
      updateChapter(updated)
    }
  }

  @Transaction
  suspend fun restoreAllData(books: List<BookEntity>, chapters: List<ChapterEntity>) {
    // Clear and restore
    books.forEach { insertBook(it) }
    insertChapters(chapters)
  }
}
