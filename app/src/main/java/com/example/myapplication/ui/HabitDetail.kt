package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.example.myapplication.Habit
import java.util.*

class HabitDetailViewModel(context: Context) : ViewModel() {
    private val storage = HabitStorage(context)

    fun getHabitByID(habitID: String): Habit =
        storage.getHabitByID(habitID)

    fun editTitle(id: UUID, title: String) {
        Log.e("DBG", "editTitle: $title")
        storage.editTitle(id, title)
    }

    fun editDescription(id: UUID, description: String) {
        Log.e("DBG", "editDescription: $description")
        storage.editDescription(id, description)
    }

    companion object {
        fun provideFactory(context: Context):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HabitDetailViewModel(context) as T
            }
        }
    }
}

@Composable
fun HabitDetail(
    nav: NavHostController,
    vm: HabitDetailViewModel,
    habitID: String
) {
    val habit = vm.getHabitByID(habitID)
    val title = remember { mutableStateOf(habit.title) }
    val description = remember { mutableStateOf(habit.description) }
    Column {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Title") },
            modifier = Modifier.onFocusChanged {
                vm.editTitle(habit.id, title.value)
            }
        )
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier.onFocusChanged {
                vm.editDescription(habit.id, description.value)
            }
        )
        Text(text = "Journal Entries:")
        habit.journalEntries.forEach {
            TextField(
                value = it.text,
                onValueChange = {},
                label = { Text("Entry @ ${it.at}") },
                readOnly = true
            )
        }
    }
}