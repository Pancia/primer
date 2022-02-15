package com.dayzerostudio.primer.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.dayzerostudio.primer.Habit
import com.dayzerostudio.primer.MyApplication
import org.burnoutcrew.reorderable.*

class HabitsListViewModel(
    context: Context,
    private val nav: NavHostController
) : ViewModel() {
    val habits = mutableStateListOf<Habit>()
    private val storage = (context as MyApplication).globals.storage

    fun refresh() {
        habits.clear()
        habits.addAll(storage.getAllTitles())
    }

    fun navToHabit(habit: Habit) {
        nav.navigate(NavRoute.HabitDetail.create(habit.id))
    }

    fun pickHabit(habit: Habit) {
        nav.navigate(NavRoute.SetTimer.create(habit.id))
    }

    fun deleteAllHabits() {
        habits.clear()
        storage.deleteAll()
    }

    fun moveHabit(from: ItemPosition, to: ItemPosition) {
        habits.move(from.index, to.index)
    }

    fun saveOrdering(_from: Int, _to: Int) {
        storage.saveHabitOrdering(habits.map { it.id })
    }

    companion object {
        fun provideFactory(context: Context, nav: NavHostController):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HabitsListViewModel(context, nav) as T
            }
        }
    }
}

@Composable
fun HabitsTab(vm: HabitsListViewModel, padding: PaddingValues) {
    vm.refresh()
    val state = rememberReorderState()
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .padding(padding)
            .fillMaxHeight(1f)
            .fillMaxWidth(1f)
            .reorderable(state, onMove = vm::moveHabit, onDragEnd = vm::saveOrdering),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        items(vm.habits) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .draggedItem(state.offsetByKey(it.id))
                    .detectReorderAfterLongPress(state),
                horizontalArrangement = Arrangement.End
            ) {
                Button(modifier = Modifier.weight(1f, true),
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
