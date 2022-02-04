package com.example.myapplication

import android.net.Uri
import java.util.*

data class JournalEntry(
    val at: String,
    val text: String?,
    val images: List<Uri> = emptyList()
)

data class Habit(
    val title: String,
    val id: UUID = UUID.randomUUID(),
    val description: String = "Default description",
    var journalEntries: List<JournalEntry> = emptyList()
)