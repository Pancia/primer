package com.example.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import com.example.myapplication.ui.myNotifChID

class AppContainer(private val applicationContext: Context) {
}

class MyApplication : Application() {
    // AppContainer instance used by the rest of classes to obtain dependencies
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
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

    fun startAlarm() {
        ringtone = RingtoneManager.getRingtone(
            this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        ringtone?.isLooping = true
        ringtone?.play()
    }

    fun stopAlarm() {
        ringtone?.stop()
    }
}