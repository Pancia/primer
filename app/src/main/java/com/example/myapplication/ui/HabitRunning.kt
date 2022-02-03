package com.example.myapplication.ui

import android.app.NotificationManager
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.example.myapplication.Habit
import com.example.myapplication.MyApplication
import java.util.*
import kotlin.math.floor

class RunningViewModel(
    private val context: Context,
    private val nav: NavHostController
) : ViewModel() {
    private lateinit var timer: CountDownTimer

    val timeLeft = mutableStateOf(0L)

    fun startCountdown(duration: Int) {
        timeLeft.value = duration.toLong()
        // TODO FIXME multiply duration by 60
        timer = object : CountDownTimer(duration * 1000L, 1000) {
            override fun onTick(millisLeft: Long) {
                timeLeft.value = floor(millisLeft / 1000.0).toLong()
            }

            override fun onFinish() {}
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    private val storage = HabitStorage(context)

    fun getHabitInfo(habitID: String): Habit =
        storage.getHabitInfoByID(habitID)

    fun editTitle(id: UUID, title: String) {
        storage.editTitle(id, title)
    }

    fun editDescription(id: UUID, desc: String) {
        storage.editDescription(id, desc)
    }

    private fun saveJournalEntry(id: UUID, entry: String) {
        storage.addJournalEntry(id, entry)
    }

    private fun cancelAlarm(habitID: UUID) {
        Alarm.stopAlarm(context, "$habitID")
        (context as MyApplication).stopAlarm()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(0)
    }

    fun done(habit: Habit, journalEntry: String) {
        cancelAlarm(habit.id)
        saveJournalEntry(habit.id, journalEntry)
        nav.navigate(NavRoute.PickHabit.create()) {
            popUpTo(NavRoute.PickHabit.route) { inclusive = true }
        }
    }

    fun cancel(habit: Habit) {
        cancelAlarm(habit.id)
        nav.navigate(NavRoute.PickHabit.create()) {
            popUpTo(NavRoute.PickHabit.route) { inclusive = true }
        }
    }

    fun snooze(habit: Habit, duration: Int) {
        cancelAlarm(habit.id)
        nav.navigate(NavRoute.SetTimer.create(habit.id, duration))
    }

    companion object {
        fun provideFactory(context: Context, nav: NavHostController):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RunningViewModel(context, nav) as T
            }
        }
    }
}

@Composable
fun HabitRunning(
    nav: NavHostController,
    vm: RunningViewModel,
    habitID: String,
    duration: Int // minutes
) {
    val habit = vm.getHabitInfo(habitID)
    val title = remember { mutableStateOf(habit.title) }
    val description = remember { mutableStateOf(habit.description) }
    val journalEntry = remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Title") },
            modifier = Modifier.onFocusChanged {
                vm.editTitle(habit.id, title.value)
            }
        )
        Text("duration: ${vm.timeLeft.value}")
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier.onFocusChanged {
                vm.editDescription(habit.id, description.value)
            }
        )
        TextField(
            value = journalEntry.value,
            onValueChange = { journalEntry.value = it },
            label = { Text("Journal Entry") }
        )
        Button(onClick = {
            vm.snooze(habit, duration)
        }) {
            Text("Snooze")
        }
        Button(onClick = {
            vm.cancel(habit)
        }) {
            Text("Cancel")
        }
        Button(onClick = {
            vm.done(habit, journalEntry.value)
        }) {
            Text("Done")
        }
    }
}