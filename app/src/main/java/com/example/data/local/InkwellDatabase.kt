package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [BookEntity::class, ChapterEntity::class],
  version = 1,
  exportSchema = false
)
abstract class InkwellDatabase : RoomDatabase() {
  abstract fun inkwellDao(): InkwellDao

  companion object {
    @Volatile
    private var INSTANCE: InkwellDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): InkwellDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          InkwellDatabase::class.java,
          "inkwell_database"
        )
        .addCallback(DatabaseCallback(scope))
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialSampleData(database.inkwellDao())
          }
        }
      }
    }

    private suspend fun populateInitialSampleData(dao: InkwellDao) {
      val sampleBookId1 = dao.insertBook(
        BookEntity(
          title = "Chronicles of the Starforged",
          coverColor = "#1B2A4A",
          coverLabel = "SCI-FI",
          synopsis = "A celestial navigator uncovers a forgotten stellar gate hidden inside the hollow core of a dormant moon.",
          createdAt = System.currentTimeMillis() - 86400000L * 3,
          updatedAt = System.currentTimeMillis() - 3600000L * 2
        )
      )

      val ch1Content = """The atmospheric dampeners hummed a low, reassuring drone as Kael adjusted the navigational prism.

Outside the reinforced observation dome, the rings of Oakhaven shimmered in violet luminescence. Dust motes of pulverized asteroid crystal drifted against the grav-shields like sparks against an obsidian hearth.

"Trajectory locked," Lyra’s voice crackled through the console comms, crisp despite three astronomical units of sensor delay. "If your readings are accurate, the rift beacon should manifest within sixty seconds."

Kael didn't answer immediately. He rested his fingertips upon the ancient copper ignition lever—a relic recovered from the third orbital expedition. In an age of synthetic consciousness and quantum matrices, there was still something profoundly grounding about the mechanical resistance of cold, unyielding alloy.

He took a slow breath and watched the star-map align."""

      val ch1Words = ch1Content.trim().split("\\s+".toRegex()).size
      val ch1Chars = ch1Content.length

      dao.insertChapter(
        ChapterEntity(
          bookId = sampleBookId1,
          title = "Chapter 1: The Ring of Oakhaven",
          content = ch1Content,
          status = "DONE",
          orderIndex = 0,
          wordCount = ch1Words,
          charCount = ch1Chars,
          createdAt = System.currentTimeMillis() - 86400000L * 3,
          updatedAt = System.currentTimeMillis() - 3600000L * 5
        )
      )

      val ch2Content = """The rift did not open with thunder. It opened like a seam parting on dark silk.

A solitary sliver of absolute zero pierced the nebula, radiating an unnatural cerulean hue that bent ambient starlight around its edges. Kael's telemetry suite flared in crimson warnings, but he muted the alarm with a swipe of his palm.

"Lyra, are you recording this spectrum?"

Static hissed. Then, a sharp intake of breath.

"That's not an ambient anomaly, Kael. The harmonic frequency matches the Archon Archives. It's an active transmission."

He stepped closer to the viewport. The light reflected in his eyes, ancient and waiting."""

      val ch2Words = ch2Content.trim().split("\\s+".toRegex()).size
      val ch2Chars = ch2Content.length

      dao.insertChapter(
        ChapterEntity(
          bookId = sampleBookId1,
          title = "Chapter 2: Cerulean Seam",
          content = ch2Content,
          status = "EDITING",
          orderIndex = 1,
          wordCount = ch2Words,
          charCount = ch2Chars,
          createdAt = System.currentTimeMillis() - 86400000L * 2,
          updatedAt = System.currentTimeMillis() - 3600000L * 2
        )
      )

      val ch3Content = """Drafting outline for Chapter 3:
- Kael prepares the shuttle EVA
- Encounter with the automated sentinel drone
- The message deciphered: coordinates leading to the Rim World sanctuary"""

      val ch3Words = ch3Content.trim().split("\\s+".toRegex()).size
      val ch3Chars = ch3Content.length

      dao.insertChapter(
        ChapterEntity(
          bookId = sampleBookId1,
          title = "Chapter 3: The Sentinel's Greeting",
          content = ch3Content,
          status = "DRAFT",
          orderIndex = 2,
          wordCount = ch3Words,
          charCount = ch3Chars,
          createdAt = System.currentTimeMillis() - 86400000L,
          updatedAt = System.currentTimeMillis() - 3600000L
        )
      )

      // Sample book 2
      val sampleBookId2 = dao.insertBook(
        BookEntity(
          title = "The Tea House on Silk Lane",
          coverColor = "#9E641E",
          coverLabel = "FANTASY",
          synopsis = "A cozy slice-of-life webnovel about brewing memory teas for wandering spirits in a mountain pass city.",
          createdAt = System.currentTimeMillis() - 86400000L * 7,
          updatedAt = System.currentTimeMillis() - 86400000L * 1
        )
      )

      val teaContent = """Steam curled from the clay kettle, carrying the earthy fragrance of roasted barley and dried chrysanthemum. 

Grandmother Lin used to say that every spirit entering the valley arrived carrying a thirst that water could never quench. They thirsted for the seasons they had left behind, the words left unsaid before the river froze over in winter.

Mei-Ling arranged three ceramic cups on the cedar tray. Outside the rain drummed softly on the eaves, a steady, comforting rhythm."""

      dao.insertChapter(
        ChapterEntity(
          bookId = sampleBookId2,
          title = "Chapter 1: The First Steep",
          content = teaContent,
          status = "DONE",
          orderIndex = 0,
          wordCount = teaContent.trim().split("\\s+".toRegex()).size,
          charCount = teaContent.length,
          createdAt = System.currentTimeMillis() - 86400000L * 7,
          updatedAt = System.currentTimeMillis() - 86400000L * 1
        )
      )
    }
  }
}
