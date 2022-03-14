package com.dayzerostudio.primer.ui

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavHostController
import com.dayzerostudio.primer.Habit
import java.util.*

class AlarmKeeper(private val context: Context) {
    private var ringtone: Ringtone? = null

    fun startAlarm(uri: Uri?) {
        ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone?.isLooping = true
        Log.e("DBG", "start: ringtone = $ringtone")
        ringtone?.play()
        vibrate(context, longArrayOf(0, 400, 300, 400, 900))
    }

    fun stopAlarm() {
        Log.e("DBG", "stop: ringtone = $ringtone")
        ringtone?.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vib = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vib.cancel()
        } else {
            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.cancel()
        }
    }
}

class AlarmViewModel(context: Context, private val nav: NavHostController) :
    MyViewModel(context, nav) {
    fun start() {
        globals.alarm.startAlarm(globals.settings.getAlarmURI())
    }

    fun stop() {
        globals.alarm.stopAlarm()
    }

    fun snooze(habitID: UUID) {
        globals.alarm.stopAlarm()
        nav.navigate(NavRoute.SetTimer.create(habitID)) {
            popUpTo(NavRoute.FullScreenAlarm.route)
        }
    }

    fun gotoHabit(habitID: UUID) {
        globals.alarm.stopAlarm()
        nav.navigate(NavRoute.HabitRunning.create(habitID, 0)) {
            popUpTo(NavRoute.FullScreenAlarm.route)
        }
    }

    fun getHabitInfo(habitID: String): Habit? =
        globals.storage.getHabitInfoByID(habitID)
}

@Composable
fun FullScreenAlarm(vm: AlarmViewModel, habitID: String) {
    DisposableEffect(habitID) {
        vm.start()
        onDispose {
            vm.stop()
        }
    }
    val habit = vm.getHabitInfo(habitID)!!
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(habit.title, style = MaterialTheme.typography.h3)
        Button(onClick = { vm.snooze(habit.id) }) {
            Text("SNOOZE")
        }
        Button(onClick = { vm.gotoHabit(habit.id) }) {
            Text("GOTO")
        }
    }
}
