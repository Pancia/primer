package com.example.myapplication.ui

import android.content.Context
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

private const val APP_NAME = "MyApplication"
private const val ENTRIES_DIR = "entries"
private const val IMAGES_DIR = "images"
private const val INFO_FILE = "info.json"

class HabitStorage(private val context: Context) {
    private val json = Klaxon().converter(uuidConverter)

    private fun rootDir() =
        File(context.externalMediaDirs.first(), APP_NAME)

    private fun storageFor(id: UUID, f: String) =
        storageFor("$id", f)

    private fun storageFor(id: String, f: String) =
        File(rootDir(), "$id/$f")

    fun getAllTitles(): List<Habit> =
        rootDir().listFiles { f -> f.isDirectory }
            ?.map { json.parse<Habit>(File(it, INFO_FILE))!! }
            ?: emptyList()

    fun getHabitInfoByID(habitID: String): Habit =
        json.parse(storageFor(habitID, INFO_FILE))!!

    fun getHabitByID(habitID: String): Habit {
        val habit = getHabitInfoByID(habitID)
        habit.journalEntries =
            (storageFor(habit.id, ENTRIES_DIR)
                .listFiles() ?: emptyArray<File>())
                .map { json.parse<JournalEntry>(it)!! }
                .sortedBy { it.at }
                .reversed()
        return habit
    }

    fun create(title: String): Habit {
        val habit = Habit(title = title)
        storageFor(habit.id, INFO_FILE)
            .apply { File(parent!!).mkdirs() }
            .writeText(json.toJsonString(habit))
        return habit
    }

    fun editTitle(id: UUID, title: String) {
        val info = storageFor(id, INFO_FILE)
        val habit = json.parse<Habit>(info)!!
        habit.title = title
        info.writeText(json.toJsonString(habit))
    }

    fun editDescription(id: UUID, description: String) {
        val info = storageFor(id, INFO_FILE)
        val habit = json.parse<Habit>(info)!!
        habit.description = description
        info.writeText(json.toJsonString(habit))
    }

    fun addJournalEntry(id: UUID, text: String, images: List<String>) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"))
        storageFor(id, "$ENTRIES_DIR/$now.json").apply {
            File(parent!!).mkdirs()
            val entry = JournalEntry(now, text, images)
            val json = json.toJsonString(entry)
            writeText(json)
        }
    }

    fun getImageOutputDirectory(habitID: UUID): File =
        storageFor(habitID, IMAGES_DIR).apply { mkdirs() }

    fun deleteAll() {
        rootDir().deleteRecursively()
    }
}
