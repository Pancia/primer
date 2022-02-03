package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlin.random.Random

val r = Random(5)

fun addRandomHabit(viewModel: HabitsListViewModel) {
    val randomTitle = "random-habit-${r.nextInt(1, 100)}"
    viewModel.createHabit(randomTitle)
}

@Composable
fun HabitsList(navController: NavHostController, habitsListViewModel: HabitsListViewModel) {
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        habitsListViewModel.habits.sortedBy { it.title }.forEach {
            Button(modifier = Modifier.padding(10.dp),
                onClick = { navController.navigate(NavRoute.HabitDetail.create(it.title)) }) {
                Text(text = it.title)
            }
        }
        Button(modifier = Modifier.padding(10.dp),
            onClick = { addRandomHabit(habitsListViewModel) }) {
            Text(text = "Add Random Habit")
        }
    }
}
