package com.example.myapplication.ui

import android.content.Context
import com.example.myapplication.Habit
import com.example.myapplication.JournalEntry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HabitStorage(private val context: Context) {
    private val habitsTitleByID = context.getSharedPreferences("habitTitleByID", 0)!!

    fun getAllTitles() = habitsTitleByID.all.map { Habit(id = UUID.fromString(it.key), title = it.value as String) }

    fun getHabitInfoByID(habitID: String): Habit {
        val id = UUID.fromString(habitID)
        val title = habitsTitleByID.getString(habitID, null)!!
        val desc = habitInfoStorageFor(id).getString("description", null)!!
        return Habit(id = id, title = title, description = desc)
    }

    fun getHabitByID(habitID: String): Habit {
        val habit = getHabitInfoByID(habitID)
        habit.journalEntries = habitJournalStorageFor(habit.id).all.entries
            .sortedBy { it.key }
            .reversed()
            .map { JournalEntry.Text(it.key, it.value as String) }
        return habit
    }

    private fun habitInfoStorageFor(id: UUID) =
        context.getSharedPreferences("$id.info", 0)!!

    private fun habitJournalStorageFor(id: UUID) =
        context.getSharedPreferences("$id.journals", 0)!!

    fun create(title: String): Habit {
        val habit = Habit(title = title)
        habitsTitleByID.edit().putString("${habit.id}", habit.title).apply()
        habitInfoStorageFor(habit.id).edit().putString("description", habit.description).apply()
        return habit
    }

    fun editTitle(id: UUID, title: String) {
        habitsTitleByID.edit().remove("$id").putString("$id", title).apply()
    }

    fun editDescription(id: UUID, description: String) {
        habitInfoStorageFor(id).edit().putString("description", description).apply()
    }

    fun addJournalEntry(id: UUID, entry: String) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"))
        habitJournalStorageFor(id).edit().putString(now, entry).apply()
    }

    fun deleteAll() {
        habitsTitleByID.all.keys.forEach {
            val id = UUID.fromString(it)
            habitInfoStorageFor(id).edit().clear().apply()
            habitJournalStorageFor(id).edit().clear().apply()
        }
        habitsTitleByID.edit().clear().apply()
    }
}
