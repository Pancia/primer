package com.example.myapplication.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.myapplication.Habit
import com.example.myapplication.JournalEntry
import com.example.myapplication.MyApplication
import java.util.*

class HabitDetailViewModel(context: Context) : ViewModel() {
    private val storage = (context as MyApplication).globals.storage

    fun getHabitByID(habitID: String): Habit =
        storage.getHabitByID(habitID)

    fun editTitle(id: UUID, title: String) {
        storage.editTitle(id, title)
    }

    fun editDescription(id: UUID, description: String) {
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
    vm: HabitDetailViewModel,
    habitID: String
) {
    val habit = vm.getHabitByID(habitID)
    val title = remember { mutableStateOf(habit.title) }
    val description = remember { mutableStateOf(habit.description) }
    Column(modifier = Modifier.fillMaxWidth(1f)) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Title") },
            textStyle = MaterialTheme.typography.h5,
            modifier = Modifier
                .onFocusChanged {
                    vm.editTitle(habit.id, title.value)
                }
                .fillMaxWidth(1f)
        )
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            textStyle = MaterialTheme.typography.h5,
            modifier = Modifier
                .onFocusChanged {
                    vm.editDescription(habit.id, description.value)
                }
                .fillMaxWidth(1f)
        )
        Text(text = "Journal Entries:", style = MaterialTheme.typography.h5)
        LazyColumn {
            items(habit.journalEntries) { entry ->
                if (entry.text?.isNotBlank() == true) {
                    TextField(
                        value = entry.text,
                        onValueChange = {},
                        label = { Text(entry.at) },
                        textStyle = MaterialTheme.typography.h5,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(1f)
                    )
                }
                if (entry.images.isNotEmpty()) {
                    LazyRow(modifier = Modifier.fillMaxWidth(1f)) {
                        items(entry.images) { uri ->
                            Image(
                                rememberImagePainter(Uri.parse(uri)),
                                contentDescription = null,
                                modifier = Modifier.size(128.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}