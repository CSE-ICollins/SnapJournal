package com.example.snapjournal.data

import kotlinx.coroutines.flow.Flow

class JournalEntryRepository(private val journalEntryDao: JournalEntryDao) {
    val entries: Flow<List<JournalEntry>> = journalEntryDao.getAllEntries()

    fun getEntry(id: Int): Flow<JournalEntry?> = journalEntryDao.getEntry(id)

    suspend fun insert(entry: JournalEntry): Long = journalEntryDao.insert(entry)

    suspend fun update(entry: JournalEntry) = journalEntryDao.update(entry)

    suspend fun delete(entry: JournalEntry) = journalEntryDao.delete(entry)
}
