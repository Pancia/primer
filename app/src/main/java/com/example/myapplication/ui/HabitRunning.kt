package com.example.myapplication.ui

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import kotlin.math.floor

class RunningViewModel : ViewModel() {
    private lateinit var timer: CountDownTimer

    val timeLeft = mutableStateOf(0L)

    fun startCountdown(duration: Int) {
        timeLeft.value = duration.toLong()
        timer = object : CountDownTimer(duration * 1000L, 1000) {
            override fun onTick(millisLeft: Long) {
                timeLeft.value = floor(millisLeft / 1000.0).toLong()
            }

            override fun onFinish() {}
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}

@Composable
fun HabitRunning(
    navController: NavHostController,
    runningViewModel: RunningViewModel,
    habitName: String,
    since: Long, // seconds
    duration: Int // minutes
) {
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(habitName)
        Text("duration: ${runningViewModel.timeLeft.value}")
        // TODO: add habit description editable field
        // TODO: add journal entry editable field
        Button(onClick = {
            // TODO: cancel old timer
            navController.navigate(NavRoute.SetTimer.create(habitName, duration))
        }) {
            Text("Snooze")
        }
        Button(onClick = {
            navController.navigate(NavRoute.PickHabit.create()) {
                popUpTo(NavRoute.PickHabit.route) { inclusive = true }
            }
        }) {
            Text("Cancel")
        }
        Button(onClick = {
            navController.navigate(NavRoute.PickHabit.create()) {
                popUpTo(NavRoute.PickHabit.route) { inclusive = true }
            }
        }) {
            Text("Done")
        }
    }
}