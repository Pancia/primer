package com.example.myapplication.ui

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.MyApplication
import java.util.*

class Alarm() : BroadcastReceiver() {
    override fun onReceive(context: Context, i: Intent) {
        val habitID = i.getStringExtra("habitID")!!
        val habit = HabitStorage(context).getHabitInfoByID(habitID) ?: return
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
            .setSilent(true)
            .setOngoing(true) // user cannot dismiss

        val ns = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ns.notify(0, builder.build())
        (context.applicationContext as MyApplication).startAlarm()
    }

    companion object {
        private fun intentFor(context: Context, habitID: String): PendingIntent {
            val i = Intent(context, Alarm::class.java)
            i.putExtra("habitID", habitID)
            return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun startAlarm(context: Context, habitID: String, time: Int) {
            val pi = intentFor(context, habitID)
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setAlarmClock(
                AlarmManager.AlarmClockInfo(
                    System.currentTimeMillis() + time * 60 * 1000,
                    null
                ), pi
            )
        }

        fun stopAlarm(context: Context, habitID: String) {
            val pi = intentFor(context, habitID)
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pi)
        }
    }
}