package com.dayzerostudio.primer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.dayzerostudio.primer.ui.AlarmKeeper
import com.dayzerostudio.primer.ui.HabitStorage
import com.dayzerostudio.primer.ui.SettingsStorage
import com.dayzerostudio.primer.ui.myNotifChID
import java.lang.Long.max
import kotlin.math.ceil
import java.util.*

private const val ACTIVE_HABIT_ID_KEY = "activeHabitID"
private const val TRIGGER_TIME_KEY = "triggerTime"

class HabitTimeKeeper(context: Context) {
    private val sp = context.getSharedPreferences(this::class.java.name, Context.MODE_PRIVATE)

    val activeHabitID =
        mutableStateOf(sp.getString(ACTIVE_HABIT_ID_KEY, null)?.let { UUID.fromString(it) })
    private val triggerTime = mutableStateOf(sp.getLong(TRIGGER_TIME_KEY, -1L))

    fun init(habitID: String, time: Int) {
        activeHabitID.value = UUID.fromString(habitID)
        sp.edit().putString(ACTIVE_HABIT_ID_KEY, habitID).apply()
        triggerTime.value = System.currentTimeMillis() + time * 60 * 1000
        sp.edit().putLong(TRIGGER_TIME_KEY, triggerTime.value).apply()
    }

    fun clear() {
        activeHabitID.value = null
        triggerTime.value = -1L
        sp.edit().remove(ACTIVE_HABIT_ID_KEY).remove(TRIGGER_TIME_KEY).apply()
    }

    fun timeLeft(): Int {
        val millisLeft = max(0L, triggerTime.value - System.currentTimeMillis())
        return ceil(millisLeft / 60 / 1000.0).toInt()
    }
}

class Globals(context: Context) {
    val alarm = AlarmKeeper(context)
    val settings = SettingsStorage(context)
    val storage = HabitStorage(context)
    val timeKeeper = HabitTimeKeeper(context)
}

class MyApplication : Application() {
    lateinit var globals: Globals

    override fun onCreate() {
        super.onCreate()
        globals = Globals(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    myNotifChID,
                    "default",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
    }
}