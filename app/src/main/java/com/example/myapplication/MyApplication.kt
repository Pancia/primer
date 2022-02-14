package com.example.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.myapplication.ui.HabitStorage
import com.example.myapplication.ui.myNotifChID
import kotlin.math.ceil
import java.util.*

class HabitTimeKeeper() {
    val activeHabitID = mutableStateOf<UUID?>(null)
    private val triggerTime = mutableStateOf<Long?>(null)

    fun init(habitID: String, time: Int) {
        activeHabitID.value = UUID.fromString(habitID)
        triggerTime.value = System.currentTimeMillis() + time * 60 * 1000
    }

    fun clear() {
        activeHabitID.value = null
        triggerTime.value = null
    }

    fun timeLeft(): Int {
        val millisLeft = (triggerTime.value?.let {
            it - System.currentTimeMillis()
        } ?: 0)
        return ceil(millisLeft / 60 / 1000.0).toInt()
    }
}

class Globals(context: Context) {
    val storage = HabitStorage(context)
    val timeKeeper = HabitTimeKeeper()
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

    private var ringtone: Ringtone? = null

    private fun vibrate(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vib = applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vib.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createWaveform
                        (pattern, 0)
                )
            )
        } else {
            val vib = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.vibrate(
                VibrationEffect.createWaveform
                    (pattern, 0)
            )
        }
    }

    fun startAlarm() {
        ringtone = RingtoneManager.getRingtone(
            this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        ringtone?.isLooping = true
        Log.e("DBG", "start: ringtone = $ringtone")
        ringtone?.play()
        vibrate(longArrayOf(0, 400, 300, 400, 900))
    }

    fun stopAlarm() {
        Log.e("DBG", "stop: ringtone = $ringtone")
        ringtone?.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vib = applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vib.cancel()
        } else {
            val vib = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.cancel()
        }
    }
}