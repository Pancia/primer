package com.example.myapplication

import android.content.Context
import java.io.File

// val file = File(context.filesDir, filename)
// var files: Array<String> = context.fileList()
// context.getDir(dirName, Context.MODE_PRIVATE)

class HabitStorage(val context: Context) {
    fun getHabits() {
        context.fileList()
        // filter only directories
    }

    fun createHabit(title: String, description: String = "TODO") {
        // TODO missing order ###
        // create directory called title
        // create description file with desc string
    }

    // TODO no idea what the API should be
    fun reorderHabit() {}

    fun editHabitDescription(description: String) {

    }

    fun addHabitNote() {}

// TODO: API to store image

    // NOTE should only remove it from the list, it should keep the description & notes
    fun deleteHabit() {}

}
