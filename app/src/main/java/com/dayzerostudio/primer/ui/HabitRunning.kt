package com.dayzerostudio.primer.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.dayzerostudio.primer.ChecklistItem
import com.dayzerostudio.primer.Habit
import com.dayzerostudio.primer.MyApplication
import java.io.File
import java.util.*

class RunningViewModel(
    val context: Context,
    private val nav: NavHostController
) : MyViewModel() {
    private lateinit var timer: CountDownTimer

    private val globals = (context as MyApplication).globals

    val timeLeft = mutableStateOf(0)

    fun startCountdown(duration: Int) {
        timeLeft.value = globals.timeKeeper.timeLeft()
        timer = object : CountDownTimer(duration * 60 * 1000L, 1000) {
            override fun onTick(millisLeft: Long) {
                timeLeft.value = globals.timeKeeper.timeLeft()
            }

            override fun onFinish() {}
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    private val storage = globals.storage

    fun getHabitInfo(habitID: String) =
        storage.getHabitInfoByID(habitID)

    fun editTitle(id: UUID, title: String) {
        storage.editTitle(id, title)
    }

    fun editDescription(id: UUID, desc: String) {
        storage.editDescription(id, desc)
    }

    fun getImageOutputDirectory(habitID: UUID): File =
        storage.getImageOutputDirectory(habitID)

    private fun saveJournalEntry(
        id: UUID,
        entry: String,
        images: List<String>,
        checklist: List<ChecklistItem>
    ) {
        storage.addJournalEntry(id, entry, images, checklist)
    }

    private fun cancelAlarm(habitID: UUID) {
        globals.timeKeeper.clear()
        Alarm.stopAlarm(context, "$habitID")
        (context as MyApplication).stopAlarm()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(0)
    }

    fun done(
        habit: Habit,
        text: String,
        images: List<String>,
        checklist: List<ChecklistItem>
    ) {
        timer.cancel()
        cancelAlarm(habit.id)
        saveJournalEntry(habit.id, text, images, checklist)
        nav.navigate(NavRoute.ListOfHabits.create()) {
            popUpTo(NavRoute.Home.route)
        }
    }

    fun cancel(habit: Habit) {
        timer.cancel()
        cancelAlarm(habit.id)
        nav.navigate(NavRoute.Home.create()) {
            popUpTo(NavRoute.Home.route)
        }
    }

    fun snooze(habit: Habit) {
        timer.cancel()
        cancelAlarm(habit.id)
        nav.navigate(NavRoute.SetTimer.create(habit.id))
    }

    fun takePicture(
        launcher: ManagedActivityResultLauncher<String, Boolean>,
        takingPicture: MutableState<Boolean>
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            takingPicture.value = true
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun HabitRunning(
    vm: RunningViewModel,
    habitID: String,
    _duration: Int
) {
    val habit = vm.getHabitInfo(habitID)!!
    val title = remember { mutableStateOf(habit.title) }
    val description = remember { mutableStateOf(habit.description) }
    val checklist = habit.checklist.toMutableStateList()
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
        Scaffold(bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = { vm.takePicture(launcher, takingPicture) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, "Take a Picture")
                }
                IconButton(onClick = { vm.cancel(habit) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Clear, "Cancel")
                }
                IconButton(onClick = {
                    vm.done(habit, journalText.value, journalImages, checklist)
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Done, "Done")
                }
            }
        }, topBar = {
            TopAppBar {
                TextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    textStyle = MaterialTheme.typography.h6,
                    modifier = Modifier.onFocusChanged {
                        vm.editTitle(habit.id, title.value)
                    }
                )
            }
        }) {
            Column(modifier = Modifier.fillMaxHeight(0.9f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { vm.snooze(habit) }) {
                        Icon(Icons.Default.Edit, "Snooze")
                        Text(
                            " duration: ${vm.timeLeft.value}",
                            style = MaterialTheme.typography.h4
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(1f)
                        .fillMaxWidth(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        TextField(
                            value = description.value,
                            onValueChange = { description.value = it },
                            label = { Text("Description") },
                            textStyle = MaterialTheme.typography.h5,
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .onFocusChanged {
                                    vm.editDescription(habit.id, description.value)
                                }
                        )
                    }
                    items(checklist, key = { it.id }) { item ->
                        Row {
                            Checkbox(item.isChecked, onCheckedChange = {
                                checklist[checklist.indexOf(item)] = item.copy(isChecked = it)
                            })
                            Text(item.text, style = MaterialTheme.typography.h5)
                        }
                    }
                    item {
                        TextField(
                            value = journalText.value,
                            onValueChange = { journalText.value = it },
                            label = { Text("Journal Entry") },
                            textStyle = MaterialTheme.typography.h5,
                            modifier = Modifier.fillMaxWidth(1f)
                        )
                    }
                    if (journalImages.isNotEmpty()) {
                        item {
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
                    }
                }
            }
        }
    }
}