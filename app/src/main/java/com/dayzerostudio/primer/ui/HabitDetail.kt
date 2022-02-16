package com.dayzerostudio.primer.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.dayzerostudio.primer.ChecklistItem
import com.dayzerostudio.primer.Habit
import com.dayzerostudio.primer.MyApplication
import org.burnoutcrew.reorderable.*
import java.util.*

enum class DetailTab {
    INFO, CHECKLIST, JOURNAL
}

class HabitDetailViewModel(val context: Context, private val nav: NavHostController) : ViewModel() {
    private val storage = (context as MyApplication).globals.storage

    val tab = mutableStateOf(DetailTab.INFO)

    fun getHabitByID(habitID: String) =
        storage.getHabitByID(habitID)

    fun editTitle(id: UUID, title: String) {
        storage.editTitle(id, title)
    }

    fun editDescription(id: UUID, description: String) {
        storage.editDescription(id, description)
    }

    fun deleteHabit(habit: Habit) {
        nav.navigate(NavRoute.ListOfHabits.create())
        storage.deleteHabit(habit)
    }

    fun viewInfo() {
        tab.value = DetailTab.INFO
    }

    fun viewChecklist() {
        tab.value = DetailTab.CHECKLIST
    }

    fun viewJournalEntries() {
        tab.value = DetailTab.JOURNAL
    }

    companion object {
        fun provideFactory(context: Context, nav: NavHostController):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HabitDetailViewModel(context, nav) as T
            }
        }
    }
}

@Composable
fun HabitDetail(
    vm: HabitDetailViewModel,
    habitID: String
) {
    val habit = vm.getHabitByID(habitID) ?: return

    Scaffold(topBar = {
        TopAppBar {
            Text(habit.id.toString().take(8))
            Row(modifier = Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { vm.deleteHabit(habit) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Habit")
                }
            }
        }
    }, bottomBar = {
        BottomAppBar {
            IconButton(onClick = { vm.viewInfo() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Info, "View Info")
            }
            IconButton(onClick = { vm.viewChecklist() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.List, "View Checklist")
            }
            IconButton(onClick = { vm.viewJournalEntries() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Edit, "View Journal Entries")
            }
        }
    }) {
        when (vm.tab.value) {
            DetailTab.INFO -> InfoTab(vm, habit, it)
            DetailTab.CHECKLIST -> {
                val newVM: ChecklistViewModel = viewModel(
                    factory = ChecklistViewModel.provideFactory(vm.context, habit)
                )
                ChecklistTab(newVM, it)
            }
            DetailTab.JOURNAL -> JournalTab(vm, habit, it)
        }
    }
}

@Composable
fun InfoTab(vm: HabitDetailViewModel, habit: Habit, padding: PaddingValues) {
    val title = remember { mutableStateOf(habit.title) }
    val description = remember { mutableStateOf(habit.description) }
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(padding)
    ) {
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
    }
}

class ChecklistViewModel(context: Context, private val habit: Habit) : ViewModel() {
    private val storage = (context as MyApplication).globals.storage

    val checklist = mutableStateListOf<ChecklistItem>()

    fun refresh() {
        checklist.clear()
        checklist.addAll(storage.checklistFor(habit.id))
    }

    fun create() {
        checklist.add(storage.addNewChecklistItem(habit))
    }

    fun editText(item: ChecklistItem, text: String) {
        if (text != item.text) {
            checklist[checklist.indexOf(item)].text = text
            storage.editChecklistItemText(habit, item.id, text)
        }
    }

    fun delete(item: ChecklistItem) {
        checklist.remove(item)
        storage.deleteChecklistItem(habit, item.id)
    }

    fun save() = storage.saveChecklist(habit, checklist)

    fun move(from: Int, to: Int) = checklist.move(from, to)

    companion object {
        fun provideFactory(context: Context, habit: Habit):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChecklistViewModel(context, habit) as T
            }
        }
    }
}

@Composable
fun ChecklistTab(
    vm: ChecklistViewModel,
    padding: PaddingValues
) {
    vm.refresh()
    val state = rememberReorderState()
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .padding(padding)
            .reorderable(state, onMove = { from, to ->
                vm.move(from.index, to.index)
            }, onDragEnd = { _, _ -> vm.save() })
    ) {
        items(vm.checklist, key = { it.id }) { item ->
            val text = remember { mutableStateOf(item.text) }
            Row {
                Icon(
                    Icons.Default.Menu, "Drag to reorder",
                    modifier = Modifier
                        .draggedItem(state.offsetByKey(item.id))
                        .detectReorder(state)
                )
                Checkbox(false, enabled = false, onCheckedChange = {})
                TextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    textStyle = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            vm.editText(item, text.value)
                        }
                )
                IconButton(onClick = { vm.delete(item) }) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
        item {
            FloatingActionButton(onClick = { vm.create() }) {
                Icon(Icons.Default.Add, "Add a checklist item")
            }
        }
    }
}

@Composable
fun JournalTab(vm: HabitDetailViewModel, habit: Habit, padding: PaddingValues) {
    LazyColumn(modifier = Modifier.padding(padding)) {
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
            if (entry.checklist.isNotEmpty()) {
                Column {
                    entry.checklist.forEach {
                        Row {
                            Checkbox(it.isChecked, enabled = false, onCheckedChange = {})
                            Text(
                                it.text,
                                style = MaterialTheme.typography.h5,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
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
