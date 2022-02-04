package com.example.myapplication

import java.util.*

data class JournalEntry(
    val at: String,
    val text: String?,
    val images: List<String> = emptyList()
)

data class Habit(
    var title: String,
    val id: UUID = UUID.randomUUID(),
    var description: String = "Default description",
    var journalEntries: List<JournalEntry> = emptyList()
)