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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.dayzerostudio.primer.ChecklistItem
import com.dayzerostudio.primer.Habit
import org.burnoutcrew.reorderable.*
import java.util.*
import kotlin.concurrent.thread

enum class DetailTab {
    INFO, CHECKLIST, JOURNAL
}

class HabitDetailViewModel(val context: Context, val nav: NavHostController) :
    MyViewModel(context, nav) {
    private val storage = globals.storage

    private val refreshKey = mutableStateOf(UUID.randomUUID())
    fun refresh(): State<UUID> {
        refreshKey.value = UUID.randomUUID()
        return refreshKey
    }

    val tab = mutableStateOf(DetailTab.INFO)

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

    fun loadHabitByID(habitID: String): IOResult<Habit> =
        IOResult.justTry {
            storage.getHabitByID(habitID)
                ?.let { IOResult.Success(it) }
                ?: IOResult.Failure("was null")
        }
}

@Composable
fun HabitDetail(
    vm: HabitDetailViewModel,
    habitID: String
) {
    val refreshKey = vm.refresh()
    val habitState = produceState<IOResult<Habit>>(IOResult.Loading, refreshKey) {
        thread { value = vm.loadHabitByID(habitID) }
    }

    when (val result = habitState.value) {
        is IOResult.Success -> {
            val habit = result.data
            Scaffold(topBar = {
                TopAppBar {
                    Text(habit.id.toString().take(8))
                    Row(
                        modifier = Modifier.fillMaxWidth(1f),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { vm.deleteHabit(habit) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Habit"
                            )
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
                    IconButton(
                        onClick = { vm.viewJournalEntries() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, "View Journal Entries")
                    }
                }
            }) {
                when (vm.tab.value) {
                    DetailTab.INFO -> InfoTab(vm, habit, it)
                    DetailTab.CHECKLIST -> {
                        val newVM: ChecklistViewModel = viewModel(
                            factory = MyViewModel.provideFactory(vm.context, vm.nav)
                        )
                        newVM.init(habit)
                        ChecklistTab(newVM, it)
                    }
                    DetailTab.JOURNAL -> JournalTab(vm, habit, it)
                }
            }
        }
        else -> {
            result.Render()
        }
    }
}

@Composable
fun InfoTab(vm: HabitDetailViewModel, habit: Habit, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(padding)
    ) {
        DebouncedTextField(
            initialValue = habit.title,
            debouncedOnValueChange = { vm.editTitle(habit.id, it) },
            scope = vm.viewModelScope,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(1f)
        )
        DebouncedTextField(
            initialValue = habit.description,
            debouncedOnValueChange = { vm.editDescription(habit.id, it) },
            scope = vm.viewModelScope,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(1f)
        )
    }
}

class ChecklistViewModel(context: Context, nav: NavHostController) : MyViewModel(context, nav) {
    private val storage = globals.storage

    val checklist = mutableStateListOf<ChecklistItem>()

    private lateinit var habit: Habit

    fun init(habit: Habit) {
        this.habit = habit
    }

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
            Row {
                Icon(
                    Icons.Default.Menu, "Drag to reorder",
                    modifier = Modifier
                        .draggedItem(state.offsetByKey(item.id))
                        .detectReorder(state)
                )
                Checkbox(false, enabled = false, onCheckedChange = {})
                DebouncedTextField(
                    initialValue = item.text,
                    scope = vm.viewModelScope,
                    debouncedOnValueChange = { vm.editText(item, it) },
                    modifier = Modifier.weight(1f)
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
            Card(modifier = Modifier.padding(10.dp)) {
                Column {
                    Text(entry.at)
                    if (entry.text?.isNotBlank() == true) {
                        TextField(
                            value = entry.text,
                            onValueChange = {},
                            textStyle = MaterialTheme.typography.h5,
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(1f)
                        )
                    }
                    if (entry.checklist.isNotEmpty()) {
                        entry.checklist.forEach {
                            Row {
                                Checkbox(it.isChecked, enabled = false, onCheckedChange = {})
                                TextField(
                                    value = it.text,
                                    onValueChange = {},
                                    label = if (it.isChecked) {
                                        { Text(it.at) }
                                    } else null,
                                    readOnly = true,
                                    textStyle = MaterialTheme.typography.h5,
                                    modifier = Modifier.weight(1f)
                                )
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
    }
}
