package com.example.myapplication

import java.util.*

// TODO notes should allow for images
data class JournalEntry(val at: String, val text: String)

data class Habit(
    val title: String,
    val id: UUID = UUID.randomUUID(),
    val description: String = "Default description",
    var journalEntries: List<JournalEntry> = emptyList()
)