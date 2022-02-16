package com.dayzerostudio.primer

import java.util.*

data class ChecklistItem(
    var text: String,
    val id: UUID = UUID.randomUUID(),
    var isChecked: Boolean = false
)

data class JournalEntry(
    val at: String,
    val text: String?,
    val images: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList()
)

data class Habit(
    var title: String,
    val id: UUID = UUID.randomUUID(),
    var description: String = "Default description",
    var checklist: List<ChecklistItem> = emptyList(),
    var journalEntries: List<JournalEntry> = emptyList()
)