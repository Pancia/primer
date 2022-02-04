package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.myapplication.MyApplication

@Composable
fun Home(context: Context, nav: NavController) {
    val globals = (context as MyApplication).globals
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { nav.navigate(NavRoute.ListOfHabits.create()) }) {
            Text(text = "List of Habits")
        }
        if (globals.timeKeeper.activeHabitID.value == null) {
            Button(onClick = { nav.navigate(NavRoute.PickHabit.create()) }) {
                Text(text = "Start Day")
            }
        } else {
            Button(onClick = {
                nav.navigate(
                    NavRoute.HabitRunning.create(
                        globals.timeKeeper.activeHabitID.value!!,
                        globals.timeKeeper.timeLeft()
                    )
                )
            }) {
                val id = globals.timeKeeper.activeHabitID.value
                val habit = globals.storage.getHabitInfoByID("$id")
                Text(text = "GOTO: ${habit.title}")
            }
        }
    }
}