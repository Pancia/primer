package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import com.example.myapplication.Habit
import com.example.myapplication.JournalEntry
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.beust.klaxon.*

private val uuidConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == UUID::class.java
    override fun toJson(value: Any): String = """{"uuid" : "${value as UUID}"}"""
    override fun fromJson(jv: JsonValue) = UUID.fromString(jv.objString("uuid"))
}

class HabitStorage(private val context: Context) {
    private val json = Klaxon().converter(uuidConverter)

    private fun rootDir() =
        File(context.externalMediaDirs.first(), "MyApplication")

    private fun habitStorageFor(id: UUID) =
        File(rootDir(), "$id") //context.getSharedPreferences("$id.info", 0)!!

    fun getAllTitles(): List<Habit> =
        rootDir().listFiles { f -> f.isDirectory }
            ?.map { json.parse<Habit>(File(it, "info.json"))!! }
            ?: emptyList()

    fun getHabitInfoByID(habitID: String): Habit =
        json.parse(File(rootDir(), "$habitID/info.json"))!!

    fun getHabitByID(habitID: String): Habit {
        val habit = getHabitInfoByID(habitID)
        habit.journalEntries =
            (File(rootDir(), "$habitID/entries")
                .listFiles() ?: emptyArray<File>())
                .map { json.parse<JournalEntry>(it)!! }
                .sortedBy { it.at }
                .reversed()
        return habit
    }

    fun create(title: String): Habit {
        val habit = Habit(title = title)
        File(rootDir(), "${habit.id}/info.json")
            .apply { File(parent!!).mkdirs() }
            .writeText(json.toJsonString(habit))
        return habit
    }

    fun editTitle(id: UUID, title: String) {
        val info = File(rootDir(), "$id/info.json")
        val habit = json.parse<Habit>(info)!!
        habit.title = title
        info.writeText(json.toJsonString(habit))
    }

    fun editDescription(id: UUID, description: String) {
        val info = File(rootDir(), "$id/info.json")
        val habit = json.parse<Habit>(info)!!
        habit.description = description
        info.writeText(json.toJsonString(habit))
    }

    fun addJournalEntry(id: UUID, text: String, images: List<String>) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"))
        File(rootDir(), "$id/entries/$now.json").apply {
            File(parent!!).mkdirs()
            val entry = JournalEntry(now, text, images)
            val json = json.toJsonString(entry)
            writeText(json)
        }
    }

    fun getImageOutputDirectory(habitID: UUID): File {
        return context.externalMediaDirs.first().let {
            File(it, "MyApplication/${habitID}/images").apply { mkdirs() }
        }
    }

    fun deleteAll() {
        File(context.externalMediaDirs.first(), "MyApplication")
            .deleteRecursively()
    }
}
