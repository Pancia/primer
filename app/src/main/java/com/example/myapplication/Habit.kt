package com.example.myapplication

import android.net.Uri
import java.util.*

sealed class JournalEntry {
    data class Text(val at: String, val text: String): JournalEntry()
    data class Image(val at: String, val uri: Uri): JournalEntry()
}

data class Habit(
    val title: String,
    val id: UUID = UUID.randomUUID(),
    val description: String = "Default description",
    var journalEntries: List<JournalEntry> = emptyList()
)