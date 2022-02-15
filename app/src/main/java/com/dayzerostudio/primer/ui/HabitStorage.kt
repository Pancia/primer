package com.dayzerostudio.primer.ui

import android.content.Context
import com.dayzerostudio.primer.Habit
import com.dayzerostudio.primer.JournalEntry
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.beust.klaxon.*
import com.dayzerostudio.primer.R
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.utils.IOUtils
import java.io.FileInputStream

private val uuidConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == UUID::class.java
    override fun toJson(value: Any): String = """{"uuid" : "${value as UUID}"}"""
    override fun fromJson(jv: JsonValue) = UUID.fromString(jv.objString("uuid"))
}

private const val ENTRIES_DIR = "entries"
private const val IMAGES_DIR = "images"
private const val INFO_FILE = "info.json"
private const val HABITS_DIR = "habits"
private const val TRASH_DIR = "trash"
private const val GLOBAL_TEXT_FILE = "global-text.txt"
private const val HABITS_ORDERING_FILE = "habits-ordering.txt"

class HabitStorage(private val context: Context) {
    private val appName = context.getString(R.string.app_name)

    private fun rootDir() =
        File(context.externalMediaDirs.first(), appName)

    private fun habitsDir() =
        File(rootDir(), HABITS_DIR)

    private fun trashDir() =
        File(rootDir(), TRASH_DIR)

    private fun globalTextFile() =
        File(rootDir(), GLOBAL_TEXT_FILE).apply { createNewFile() }

    private fun orderingFile() =
        File(rootDir(), HABITS_ORDERING_FILE).apply { createNewFile() }

    private fun storageFor(id: UUID) =
        File(habitsDir(), "$id")

    private fun storageFor(id: UUID, f: String) =
        storageFor("$id", f)

    private fun storageFor(id: String, f: String) =
        File(habitsDir(), "$id/$f")

    private val json = Klaxon().converter(uuidConverter)

    private fun getHabitOrdering() =
        orderingFile().readLines().map { UUID.fromString(it) }

    fun saveHabitOrdering(ordering: List<UUID>) =
        orderingFile().writeText(ordering.joinToString("\n"))

    fun getAllTitles(): List<Habit> =
        getHabitOrdering()
            .map { json.parse<Habit>(storageFor(it, INFO_FILE))!! }

    fun getHabitInfoByID(habitID: String): Habit? =
        storageFor(habitID, INFO_FILE).let {
            if (it.exists()) json.parse(it) else null
        }

    fun getHabitByID(habitID: String): Habit? {
        val habit = getHabitInfoByID(habitID)
        if (habit == null) return habit
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
        val ordering = getHabitOrdering()
        saveHabitOrdering(ordering.plus(habit.id))
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

    fun getGlobalText() =
        globalTextFile().readText()

    fun editGlobalText(text: String) =
        globalTextFile().writeText(text)

    fun deleteAll() {
        habitsDir().deleteRecursively()
    }

    fun deleteHabit(habit: Habit) {
        storageFor(habit.id).apply {
            copyRecursively(target = File(trashDir(), habit.id.toString()).ensureExists(), overwrite = true)
                    && deleteRecursively()
        }
        val ordering = getHabitOrdering()
        saveHabitOrdering(ordering.filterNot { it == habit.id })
    }

    private fun exportsDir(): File =
        File(rootDir(), "exports").apply { mkdirs() }

    private fun addToZip(out: ZipArchiveOutputStream, f: File, name: String) {
        out.apply {
            val entry = createArchiveEntry(f, name)
            putArchiveEntry(entry)
            IOUtils.copy(FileInputStream(f), out)
            out.closeArchiveEntry()
        }
    }

    fun createZip(): File {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"))
        val outFile = File(exportsDir(), "exported-habits.${now}.zip")
        val out = ZipArchiveOutputStream(outFile)
        addToZip(out, globalTextFile(), GLOBAL_TEXT_FILE)
        addToZip(out, orderingFile(), HABITS_ORDERING_FILE)
        getAllTitles().forEach { habit ->
            val info = storageFor(habit.id, INFO_FILE)
            addToZip(out, info, "${habit.title}/info.json")
            val entries = storageFor(habit.id, ENTRIES_DIR)
            (entries.listFiles() ?: emptyArray()).forEach { entry ->
                addToZip(out, entry, "${habit.title}/entries/${entry.name}")
            }
            val images = storageFor(habit.id, IMAGES_DIR)
            (images.listFiles() ?: emptyArray()).forEach { image ->
                addToZip(out, image, "${habit.title}/images/${image.name}")
            }
        }
        out.finish()
        return outFile
    }
}

private fun File.ensureExists(): File {
    if (!exists()) createNewFile()
    return this
}
