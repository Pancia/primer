package com.dayzerostudio.primer.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.dayzerostudio.primer.ChecklistItem
import com.dayzerostudio.primer.Habit
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow

class RunningViewModel(
    val context: Context,
    private val nav: NavHostController
) : MyViewModel(context, nav) {
    private lateinit var timer: CountDownTimer

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
        checklist: List<ChecklistItem>,
        recordings: List<String>
    ) {
        storage.addJournalEntry(id, entry, images, checklist, recordings)
    }

    private fun cancelAlarm(habitID: UUID) {
        globals.timeKeeper.clear()
        Alarm.stopAlarm(context, "$habitID")
        globals.alarm.stopAlarm()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(0)
    }

    private fun clearEntryCache() {
        context.getCache().edit()
            .remove("entry.checklist")
            .remove("entry.journalText")
            .remove("entry.journalImages")
            .remove("entry.recordings")
            .apply()
    }

    fun done(
        habit: Habit,
        text: String,
        images: List<String>,
        checklist: List<ChecklistItem>,
        recordings: List<String>
    ) {
        timer.cancel()
        cancelAlarm(habit.id)
        clearEntryCache()
        saveJournalEntry(habit.id, text, images, checklist, recordings)
        nav.navigate(NavRoute.ListOfHabits.create()) {
            popUpTo(NavRoute.Home.route)
        }
    }

    fun cancel(habit: Habit) {
        timer.cancel()
        cancelAlarm(habit.id)
        clearEntryCache()
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
        if (context.hasPermission(Manifest.permission.CAMERA)) {
            takingPicture.value = true
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    private var recorder: MediaRecorder? = null
    val isRecordingAudio = mutableStateOf(false)
    private val recordingPath = mutableStateOf<String?>(null)

    private fun startRecording(fileName: String) {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            setOutputFile(fileName)
            try {
                prepare()
            } catch (e: Exception) {
                Log.e("DBG", "recorder failed", e)
            }
            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    fun toggleIsRecordingAudio(
        habit: Habit,
        requestAudio: ManagedActivityResultLauncher<String, Boolean>,
        recordings: MutableState<List<String>>
    ) {
        if (isPlayingAudio.value != null) return
        if (isRecordingAudio.value) {
            stopRecording()
            isRecordingAudio.value = false
            recordings.value = recordings.value + recordingPath.value!!
            recordingPath.value = null
        } else {
            if (context.hasPermission(Manifest.permission.RECORD_AUDIO)) {
                isRecordingAudio.value = true
                val fileName = storage.pathToRecordingFor(habit)
                recordingPath.value = fileName
                Log.e("DBG", "recording to: $fileName")
                startRecording(fileName)
            } else {
                requestAudio.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private var player: MediaPlayer? = null
    val isPlayingAudio = mutableStateOf<String?>(null)

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startPlaying(recording: String) {
        isPlayingAudio.value = recording
        Log.e("DBG", "playing: $recording")
        player = MediaPlayer().apply {
            try {
                setDataSource(FileInputStream(recording).fd)
                prepare()
                start()
            } catch (e: Exception) {
                Log.e("DBG", "playing recording failed", e)
            }
        }
        player?.setOnCompletionListener {
            player?.release()
            player = null
            isPlayingAudio.value = null
        }
    }

    fun toggleIsPlayingRecording(recording: String) {
        if (isRecordingAudio.value) return
        if (isPlayingAudio.value != null) {
            stopPlaying()
            if (isPlayingAudio.value != recording) {
                startPlaying(recording)
            } else {
                isPlayingAudio.value = null
            }
        } else {
            startPlaying(recording)
        }
    }
}

private fun Context.hasPermission(permission: String): Boolean =
    PackageManager.PERMISSION_GRANTED ==
            ContextCompat.checkSelfPermission(this, permission)

@Composable
fun HabitRunning(
    vm: RunningViewModel,
    habitID: String,
    _duration: Int
) {
    val habit = vm.getHabitInfo(habitID)!!
    val checklist = rememberJsonListPreference(
        keyName = "entry.checklist",
        initialValue = habit.checklist
    )
    val recordings = rememberJsonListPreference<String>(
        keyName = "entry.recordings",
        initialValue = emptyList()
    )
    val journalText = rememberStringPreference(
        keyName = "entry.journalText",
        initialValue = ""
    )
    val journalImages = rememberJsonListPreference<String>(
        keyName = "entry.journalImages",
        initialValue = emptyList()
    )

    val takingPicture = remember { mutableStateOf(false) }

    val requestCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takingPicture.value = true
        } else {
            // Permission Denied
        }
    }
    val requestAudio = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            vm.isRecordingAudio.value = true
        } else {
            // Permission Denied
        }
    }

    if (takingPicture.value) {
        CameraView(onImageCaptured = { uri, fromGallery ->
            Log.e("DBG", "uri: $uri, $fromGallery")
            takingPicture.value = false
            journalImages.value = journalImages.value + "$uri"
        }, onError = { e ->
            Log.e("DBG", "err: $e")
        }, getOutputDirectory = { vm.getImageOutputDirectory(habit.id) })
    } else {
        Scaffold(bottomBar = {
            BottomAppBar {
                IconButton(
                    onClick = { vm.takePicture(requestCamera, takingPicture) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, "Take a Picture")
                }
                IconButton(onClick = { vm.cancel(habit) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Clear, "Cancel")
                }
                IconButton(onClick = {
                    vm.done(
                        habit,
                        journalText.value,
                        journalImages.value,
                        checklist.value,
                        recordings.value
                    )
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Done, "Done")
                }
            }
        }, topBar = {
            TopAppBar {
                DebouncedTextField(
                    initialValue = habit.title,
                    debouncedOnValueChange = { vm.editTitle(habit.id, it) },
                    scope = vm.viewModelScope,
                    textStyle = MaterialTheme.typography.h6,
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
                            " ${vm.timeLeft.value} minutes",
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
                        DebouncedTextField(
                            initialValue = habit.description,
                            debouncedOnValueChange = { vm.editDescription(habit.id, it) },
                            scope = vm.viewModelScope,
                            label = { Text("Description") },
                            textStyle = MaterialTheme.typography.h5,
                            modifier = Modifier.fillMaxWidth(1f)
                        )
                    }
                    items(checklist.value, key = { it.id }) { item ->
                        Row {
                            Checkbox(item.isChecked, onCheckedChange = { checked ->
                                val newList = checklist.value.toMutableList()
                                newList[newList.indexOf(item)] = item.copy(isChecked = checked)
                                checklist.value = newList
                            }, modifier = Modifier.size(40.dp))
                            Text(item.text, style = MaterialTheme.typography.h5)
                        }
                    }
                    items(recordings.value) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vm.toggleIsPlayingRecording(it) }) {
                                if (vm.isPlayingAudio.value == it)
                                    Icon(Icons.Default.Close, "stop playing recording")
                                else
                                    Icon(Icons.Default.PlayArrow, "start playing recording")
                            }
                            Text(File(it).humanSize())
                            Text(File(it).name)
                        }
                    }
                    item {
                        Row {
                            IconButton(onClick = {
                                vm.toggleIsRecordingAudio(
                                    habit,
                                    requestAudio,
                                    recordings
                                )
                            }) {
                                if (vm.isRecordingAudio.value)
                                    Icon(Icons.Default.Done, "stop recording audio")
                                else
                                    Icon(Icons.Default.Call, "start recording audio")
                            }
                            // TODO length / size of recording
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
                    if (journalImages.value.isNotEmpty()) {
                        item {
                            LazyRow(
                                modifier = Modifier.scrollable(
                                    orientation = Orientation.Horizontal,
                                    enabled = true,
                                    state = ScrollableState { it }
                                )
                            ) {
                                items(journalImages.value) {
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

fun File.humanSize(): String {
    val size = length()
    val i = floor(log2(size.toDouble()) / log2(1024.0));
    val suffix = arrayOf("B", "kB", "MB", "GB", "TB")[i.toInt()]
    return "%.1f %s".format(size / 1024.0.pow(i), suffix)
}
