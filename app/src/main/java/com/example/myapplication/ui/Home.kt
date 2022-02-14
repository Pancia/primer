package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.myapplication.MyApplication
import com.example.myapplication.sendZipToServer
import okhttp3.Dispatcher
import kotlin.concurrent.thread

@Composable
fun Home(context: Context, nav: NavController) {
    val globals = (context as MyApplication).globals
    var showOverflow by remember { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("MyApp") }, actions = {
            IconButton(onClick = { showOverflow = !showOverflow }) {
                Icon(Icons.Default.MoreVert, "More")
            }
            DropdownMenu(expanded = showOverflow,
                onDismissRequest = { showOverflow = false }) {
                DropdownMenuItem(onClick = {
                    thread {
                        val z = globals.storage.createZip()
                        sendZipToServer(z)
                    }
                }) {
                    Icon(Icons.Default.Share, "Export")
                    Text("Export")
                }
            }
        })
    }) {
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
}