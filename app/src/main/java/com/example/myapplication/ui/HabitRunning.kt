package com.example.myapplication.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.myapplication.Habit
import com.example.myapplication.MyApplication
import java.io.File
import java.util.*
import kotlin.math.floor

class RunningViewModel(
    val context: Context,
    private val nav: NavHostController
) : ViewModel() {
    private lateinit var timer: CountDownTimer

    private val globals = (context as MyApplication).globals

    val timeLeft = mutableStateOf(0)

    fun startCountdown(duration: Int) {
        timeLeft.value = globals.timer.timeLeft()
        timer = object : CountDownTimer(duration * 60 * 1000L, 1000) {
            override fun onTick(millisLeft: Long) {
                timeLeft.value = globals.timer.timeLeft()
            }
            override fun onFinish() {}
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    private val storage = globals.storage

    fun getHabitInfo(habitID: String): Habit =
        storage.getHabitInfoByID(habitID)

    fun editTitle(id: UUID, title: String) {
        storage.editTitle(id, title)
    }

    fun editDescription(id: UUID, desc: String) {
        storage.editDescription(id, desc)
    }

    fun getImageOutputDirectory(habitID: UUID): File =
        storage.getImageOutputDirectory(habitID)

    private fun saveJournalEntry(id: UUID, entry: String, images: List<String>) {
        storage.addJournalEntry(id, entry, images)
    }

    private fun cancelAlarm(habitID: UUID) {
        globals.timer.clear()
        Alarm.stopAlarm(context, "$habitID")
        (context as MyApplication).stopAlarm()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(0)
    }

    fun done(habit: Habit, text: String, images: MutableList<String>) {
        timer.cancel()
        cancelAlarm(habit.id)
        saveJournalEntry(habit.id, text, images)
        nav.navigate(NavRoute.PickHabit.create()) {
            popUpTo(NavRoute.Home.route)
        }
    }

    fun cancel(habit: Habit) {
        timer.cancel()
        cancelAlarm(habit.id)
        nav.navigate(NavRoute.PickHabit.create()) {
            popUpTo(NavRoute.Home.route)
        }
    }

    fun snooze(habit: Habit) {
        timer.cancel()
        cancelAlarm(habit.id)
        nav.navigate(NavRoute.SetTimer.create(habit.id))
    }

    companion object {
        fun provideFactory(context: Context, nav: NavHostController):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RunningViewModel(context, nav) as T
            }
        }
    }
}

@Composable
fun HabitRunning(
    vm: RunningViewModel,
    habitID: String,
    _duration: Int
) {
    val habit = vm.getHabitInfo(habitID)
    val title = remember { mutableStateOf(habit.title) }
    val description = remember { mutableStateOf(habit.description) }
    val journalText = remember { mutableStateOf("") }
    val journalImages = remember { mutableListOf<String>() }
    val takingPicture = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takingPicture.value = true
        } else {
            // Permission Denied
        }
    }

    if (takingPicture.value) {
        CameraView(onImageCaptured = { uri, fromGallery ->
            Log.e("DBG", "uri: $uri, $fromGallery")
            takingPicture.value = false
            journalImages.add("$uri")
        }, onError = { e ->
            Log.e("DBG", "err: $e")
        }, getOutputDirectory = { vm.getImageOutputDirectory(habit.id) })
    } else {
        Column(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = title.value,
                onValueChange = { title.value = it },
                label = { Text("Title") },
                modifier = Modifier.onFocusChanged {
                    vm.editTitle(habit.id, title.value)
                }
            )
            Text("duration: ${vm.timeLeft.value}")
            TextField(
                value = description.value,
                onValueChange = { description.value = it },
                label = { Text("Description") },
                modifier = Modifier.onFocusChanged {
                    vm.editDescription(habit.id, description.value)
                }
            )
            TextField(
                value = journalText.value,
                onValueChange = { journalText.value = it },
                label = { Text("Journal Entry") }
            )
            if (journalImages.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.scrollable(
                        orientation = Orientation.Horizontal,
                        enabled = true,
                        state = ScrollableState { it }
                    )
                ) {
                    items(journalImages) {
                        Image(
                            rememberImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier.size(128.dp)
                        )
                    }
                }
            }
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(
                        vm.context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    takingPicture.value = true
                } else {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Text("Take Picture")
            }
            Button(onClick = {
                vm.snooze(habit)
            }) {
                Text("Snooze")
            }
            Button(onClick = {
                vm.cancel(habit)
            }) {
                Text("Cancel")
            }
            Button(onClick = {
                vm.done(habit, journalText.value, journalImages)
            }) {
                Text("Done")
            }
        }
    }
}