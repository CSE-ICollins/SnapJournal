package com.example.snapjournal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val imagePath: String,
    val date: Long,
    val mediaType: String = MediaType.PHOTO.name
)

enum class MediaType {
    PHOTO,
    VIDEO
}
