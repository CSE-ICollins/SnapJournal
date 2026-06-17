package com.example.snapjournal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.snapjournal.data.JournalEntry
import com.example.snapjournal.data.JournalEntryRepository
import com.example.snapjournal.data.MediaType
import com.example.snapjournal.data.SnapJournalDatabase
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(private val repository: JournalEntryRepository) : ViewModel() {
    val entries: StateFlow<List<JournalEntry>> = repository.entries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun entry(id: Int): Flow<JournalEntry?> = repository.getEntry(id)

    fun addEntry(
        title: String,
        description: String,
        imagePath: String,
        mediaType: MediaType,
        date: Long,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insert(
                JournalEntry(
                    title = title.trim(),
                    description = description.trim(),
                    imagePath = imagePath,
                    date = date,
                    mediaType = mediaType.name
                )
            )
            onSaved()
        }
    }

    fun updateEntry(
        entry: JournalEntry,
        title: String,
        description: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.update(
                entry.copy(
                    title = title.trim(),
                    description = description.trim()
                )
            )
            onSaved()
        }
    }

    fun deleteEntry(entry: JournalEntry, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.delete(entry)
            File(entry.imagePath).delete()
            onDeleted()
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = SnapJournalDatabase.getDatabase(context)
                JournalViewModel(
                    JournalEntryRepository(database.journalEntryDao())
                )
            }
        }
    }
}
