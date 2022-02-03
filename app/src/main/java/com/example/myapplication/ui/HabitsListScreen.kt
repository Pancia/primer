package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

var n = 1

fun addRandomHabit(vm: HabitsListViewModel) {
    val randomTitle = "random-habit-${n++}"
    vm.createHabit(randomTitle)
}

@Composable
fun HabitsList(nav: NavHostController, vm: HabitsListViewModel) {
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Button(modifier = Modifier.padding(10.dp),
            onClick = { vm.deleteAllHabits() }) {
            Text(text = "Delete All Habits")
        }
        vm.habits.sortedBy { it.title }.forEach {
            Button(modifier = Modifier.padding(10.dp),
                onClick = { nav.navigate(NavRoute.HabitDetail.create(it.id)) }) {
                Text(text = it.title)
            }
        }
        Button(modifier = Modifier.padding(10.dp),
            onClick = { addRandomHabit(vm) }) {
            Text(text = "Add Random Habit")
        }
    }
}
