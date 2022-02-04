package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.example.myapplication.Habit
import com.example.myapplication.MyApplication
import java.util.*

const val myNotifChID = "MY_CHANNEL"

class TimerViewModel(
    private val context: Context,
    private val nav: NavHostController,
    private val habitID: String,
    initialDuration: Int?
) : ViewModel() {
    val isAddition = mutableStateOf(true)
    fun toggleAddition() {
        isAddition.value = !isAddition.value
    }

    val time = mutableStateOf(initialDuration ?: 0) // in minutes
    fun addTime(i: Int) {
        time.value = time.value + if (isAddition.value) i else -i
    }

    private val storage = (context as MyApplication).globals.storage
    private val globals = (context as MyApplication).globals

    fun getHabitInfo(habitID: String): Habit =
        storage.getHabitInfoByID(habitID)

    fun start(time: Int) {
        globals.timer.init(habitID, time)
        Alarm.startAlarm(context, habitID, time)
        nav.navigate(NavRoute.HabitRunning.create(UUID.fromString(habitID), time)) {
            popUpTo(NavRoute.Home.route)
        }
    }

    companion object {
        fun provideFactory(
            context: Context,
            nav: NavHostController,
            habitID: String,
            initialDuration: Int?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimerViewModel(context, nav, habitID, initialDuration) as T
            }
        }
    }
}

@Composable
fun TimeButton(vm: TimerViewModel, time: Int) =
    Button(onClick = { vm.addTime(time) }) {
        Text("$time")
    }

@Composable
fun HabitTimer(
    vm: TimerViewModel,
    habitID: String
) {
    val habit = vm.getHabitInfo(habitID)
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(habit.title)
        Text("${vm.time.value} minutes")
        Row() {
            Switch(checked = vm.isAddition.value,
                onCheckedChange = { vm.toggleAddition() })
            Text(if (vm.isAddition.value) "Add" else "Sub")
        }
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeButton(vm, 1)
            TimeButton(vm, 5)
            TimeButton(vm, 15)
        }
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeButton(vm, 30)
            TimeButton(vm, 60)
        }
        Button(onClick = { vm.start(vm.time.value) }) {
            Text(text = "Start")
        }
    }
}