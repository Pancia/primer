package com.dayzerostudio.primer.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.dayzerostudio.primer.MyApplication
import java.util.*

typealias Minutes = Int

const val myNotifChID = "MY_CHANNEL"

class TimerViewModel(
    private val context: Context,
    private val nav: NavHostController
) : MyViewModel(context, nav) {
    private lateinit var habitID: String
    lateinit var time: MutableState<String>

    fun init(habitID: String, duration: String?) {
        this.habitID = habitID
        this.time = mutableStateOf(duration ?: "")
    }

    private val storage = globals.storage

    fun getHabitInfo(habitID: String) =
        storage.getHabitInfoByID(habitID)

    fun addDigit(i: Int) {
        if (time.value.length < 4) {
            time.value = time.value + "$i"
        }
    }

    fun timeBackspace() {
        if (time.value.isNotEmpty()) {
            time.value = time.value.dropLast(1)
        }
    }

    fun clearTime() {
        time.value = ""
    }

    fun start() {
        if (time.value.isBlank()) return
        val (hours, minutes) = time.value.padStart(4, '0').chunked(2).map { it.toInt() }
        val duration: Minutes = hours * 60 + minutes
        globals.timeKeeper.init(habitID, duration)
        Alarm.startAlarm(context, habitID, duration)
        nav.navigate(NavRoute.HabitRunning.create(UUID.fromString(habitID), duration)) {
            popUpTo(NavRoute.Home.route)
        }
    }
}

@Composable
fun TimeButton(vm: TimerViewModel, time: Int) =
    Button(onClick = { vm.addDigit(time) }) {
        Text("$time")
    }

@Composable
fun HabitTimer(
    vm: TimerViewModel,
    habitID: String
) {
    val habit = vm.getHabitInfo(habitID)!!
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(habit.title, style = MaterialTheme.typography.h5)
        val (hours, minutes) = vm.time.value.padStart(4, '0').chunked(2)
        Text("${hours}h ${minutes}m", style = MaterialTheme.typography.h3)
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeButton(vm, 1)
            TimeButton(vm, 2)
            TimeButton(vm, 3)
        }
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeButton(vm, 4)
            TimeButton(vm, 5)
            TimeButton(vm, 6)
        }
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimeButton(vm, 7)
            TimeButton(vm, 8)
            TimeButton(vm, 9)
        }
        Row(
            modifier = Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton({ vm.clearTime() }) {
                Icon(Icons.Default.Delete, "Clear")
            }
            TimeButton(vm, 0)
            IconButton({ vm.timeBackspace() }) {
                Icon(Icons.Default.ArrowBack, "backspace")
            }
        }
        IconButton(onClick = { vm.start() }) {
            if (vm.time.value.isNotBlank()) {
                Icon(Icons.Filled.PlayArrow, "Start")
            }
        }
    }
}