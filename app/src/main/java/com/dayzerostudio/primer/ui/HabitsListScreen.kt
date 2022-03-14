package com.dayzerostudio.primer.ui

import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.dayzerostudio.primer.Habit
import com.dayzerostudio.primer.MyApplication
import org.burnoutcrew.reorderable.*
import java.util.*
import kotlin.concurrent.thread

typealias ResultHabits = IOResult<MutableList<Habit>>

class HabitsListViewModel(
    public val context: Context,
    private val nav: NavHostController
) : ViewModel() {
    private val storage = (context as MyApplication).globals.storage

    private val refreshKey = mutableStateOf(UUID.randomUUID())
    fun refresh(): State<UUID> {
        refreshKey.value = UUID.randomUUID()
        return refreshKey
    }

    fun navToHabit(habit: Habit) {
        nav.navigate(NavRoute.HabitDetail.create(habit.id))
    }

    fun pickHabit(habit: Habit) {
        nav.navigate(NavRoute.SetTimer.create(habit.id))
    }

    fun moveHabit(habits: MutableList<Habit>, from: ItemPosition, to: ItemPosition) {
        habits.move(from.index, to.index)
    }

    fun saveOrdering(habits: MutableList<Habit>) {
        storage.saveHabitOrdering(habits.map { it.id })
    }

    fun getHabitList() = IOResult.tryLoad { storage.getAllTitles().toMutableList() }
}

@Composable
fun HabitsTab(vm: HabitsListViewModel, padding: PaddingValues) {
    val refreshKey = vm.refresh()
    val habitsState = produceState<ResultHabits>(IOResult.Loading, refreshKey) {
        thread { value = vm.getHabitList() }
    }
    when (val result = habitsState.value) {
        is IOResult.Success -> {
            val habits = result.data
            val state = rememberReorderState()
            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxHeight(1f)
                    .fillMaxWidth(1f)
                    .reorderable(state,
                        onMove = { f, t -> vm.moveHabit(habits, f, t) },
                        onDragEnd = { _, _ -> vm.saveOrdering(habits) }
                    ),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                items(result.data, key = { it.id }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .draggedItem(state.offsetByKey(it.id))
                            .detectReorderAfterLongPress(state),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(modifier = Modifier
                            .weight(1f, true)
                            .padding(5.dp),
                            onClick = { vm.navToHabit(it) }) {
                            Text(text = it.title)
                        }
                        IconButton(onClick = { vm.pickHabit(it) }) {
                            Icon(Icons.Default.Send, "Start")
                        }
                    }
                }
            }
        }
        else -> {
            result.Render()
        }
    }
}