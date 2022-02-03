package com.example.myapplication.ui

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.widget.ToggleButton
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.example.myapplication.Habit
import com.example.myapplication.MainActivity
import com.example.myapplication.MyApplication

const val myNotifChID = "MY_CHANNEL"

class Alarm() : BroadcastReceiver() {
    override fun onReceive(context: Context, i: Intent) {
        val habitID = i.getStringExtra("habitID")!!
        val habit = HabitStorage(context).getHabitInfoByID(habitID)
        Toast.makeText(context, "ALARM RECEIVED: ${habit.title}", Toast.LENGTH_LONG).show()

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigateTo", NavRoute.HabitRunning.create(habit.id, 0))
        }
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, myNotifChID)
            .setSmallIcon(android.R.drawable.btn_star)
            .setContentTitle("habit: ${habit.title}")
            .setContentText("MY TEXT")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // user cannot dismiss

        val ns = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ns.notify(0, builder.build())
        (context.applicationContext as MyApplication).startAlarm()
    }
}

class TimerViewModel(
    private val context: Context,
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

    fun startAlarm() {
        val i = Intent(context, Alarm::class.java)
        i.putExtra("habitID", habitID)
        val pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                System.currentTimeMillis() + time.value * 1000, // TODO: (* 60) seconds in a minute
                null
            ), pi
        )
    }

    private val storage = HabitStorage(context)

    fun getHabitInfo(habitID: String): Habit =
        storage.getHabitInfoByID(habitID)

    companion object {
        fun provideFactory(context: Context, habitID: String, initialDuration: Int?):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimerViewModel(context, habitID, initialDuration) as T
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
    nav: NavHostController,
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
        Button(onClick = {
            vm.startAlarm()
            nav.navigate(
                NavRoute.HabitRunning.create(
                    habit.id,
                    vm.time.value
                )
            )
        }) {
            Text(text = "Start")
        }
    }
}