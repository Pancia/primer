package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HabitsList(vm: HabitsListViewModel) {
    vm.refresh()
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            val habit = vm.createHabit("TempTitle")
            vm.navToHabit(habit)
        }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create a new Habit")
        }
    }, floatingActionButtonPosition = FabPosition.Center) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            items(vm.habits.sortedBy { it.title }) {
                Button(modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(1f),
                    onClick = { vm.navToHabit(it) }) {
                    Text(text = it.title)
                }
            }
        }
    }
}
