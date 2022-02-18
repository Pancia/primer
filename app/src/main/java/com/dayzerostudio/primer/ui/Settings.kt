package com.dayzerostudio.primer.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

private fun Context.getSettings() = getSharedPreferences("settings", Context.MODE_PRIVATE)

class SettingsStorage(context: Context) {
    private val sp = context.getSettings()

    fun getAlarmURI(): Uri? =
        sp.getString("alarm_uri", null)?.let(Uri::parse)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
}

@Composable
fun rememberStringSetting(keyName: String): MutableState<String?> {
    val currentState: MutableState<String?> = remember { mutableStateOf(null) }
    val context = LocalContext.current
    val sp = context.getSettings()
    sp.getString(keyName, null)?.let {
        currentState.value = it
    }
    return object : MutableState<String?> {
        override var value: String?
            get() = currentState.value
            set(v) {
                currentState.value = v
                sp.edit().putString(keyName, v).apply()
            }

        override fun component1() = value
        override fun component2(): (String?) -> Unit = { value = it }
    }
}

class SettingsViewModel(context: Context, nav: NavHostController) : MyViewModel(context, nav) {
}

// https://developer.android.com/training/basics/intents/result#custom
class PickRingtone : ActivityResultContract<Int, Uri?>() {
    override fun createIntent(context: Context, ringtoneType: Int) =
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType)
        }

    override fun parseResult(resultCode: Int, result: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return result?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
    }
}

@Composable
fun Settings(vm: SettingsViewModel) {
    val alarmUri = rememberStringSetting("alarm_uri")
    val pickAlarmSound = rememberLauncherForActivityResult(PickRingtone()) { uri ->
        if (uri != null) {
            alarmUri.value = "$uri"
        }
    }
    Column {
        Card {
            val alarmName = alarmUri.value?.let(Uri::parse)?.getQueryParameter("title") ?: alarmUri.value
            Column {
                Text("Alarm: $alarmName")
                Button(onClick = { pickAlarmSound.launch(RingtoneManager.TYPE_ALARM) }) {
                    Text("Change Alarm Sound")
                }
            }
        }
    }
}
