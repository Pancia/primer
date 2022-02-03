package com.example.myapplication.ui

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.MainActivity
import com.example.myapplication.MyApplication

const val myNotifChID = "MY_CHANNEL"

class Alarm() : BroadcastReceiver() {
    override fun onReceive(context: Context, i: Intent) {
        Toast.makeText(context, "ALARM RECEIVED: ${i.getStringExtra("habit")}", Toast.LENGTH_LONG).show()
        // on click go to this screen (how?)

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigateTo", NavRoute.HabitRunning.create(i.getStringExtra("habit")!!, 0, 0))
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, myNotifChID)
            .setSmallIcon(android.R.drawable.btn_star)
            .setContentTitle("MY TITLE")
            .setContentText("MY TEXT")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // cannot dismiss

        val ns = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ns.notify(0, builder.build())
        (context.applicationContext as MyApplication).startAlarm()
    }
}

class TimerViewModel(private val context: Context, private val habitName: String, initialDuration: Int?) : ViewModel() {
    val time = mutableStateOf(initialDuration ?: 0) // in minutes
    fun addTime(i: Int) {
        time.value = time.value + i
    }

    fun startAlarm() {
        val i = Intent(context, Alarm::class.java)
        i.putExtra("habit", habitName)
        val pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                System.currentTimeMillis() + time.value * 1000, // TODO: (* 60) seconds in a minute
                null
            ), pi
        )
    }

    companion object {
        fun provideFactory(context: Context, habitName: String, initialDuration: Int?):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimerViewModel(context, habitName, initialDuration) as T
            }
        }
    }
}

@Composable
fun HabitTimer(
    navController: NavHostController,
    timerViewModel: TimerViewModel,
    habitName: String
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(habitName)
        Text("${timerViewModel.time.value} minutes")
        Row(modifier = Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { timerViewModel.addTime(1) }) {
                Text("1")
            }
            Button(onClick = { timerViewModel.addTime(5) }) {
                Text("5")
            }
            Button(onClick = { timerViewModel.addTime(15) }) {
                Text("15")
            }
        }
        Row(modifier = Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { timerViewModel.addTime(30) }) {
                Text("30")
            }
            Button(onClick = { timerViewModel.addTime(60) }) {
                Text("60")
            }
        }
        Button(onClick = {
            val now = System.currentTimeMillis() / 1000
            timerViewModel.startAlarm()
            navController.navigate(
                NavRoute.HabitRunning.create(
                    habitName,
                    now,
                    timerViewModel.time.value
                )
            )
        }) {
            Text(text = "Start")
        }
    }
}