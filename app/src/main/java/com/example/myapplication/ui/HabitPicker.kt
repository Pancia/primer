package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HabitPicker(nav: NavHostController, vm: HabitsListViewModel) {
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        vm.habits.sortedBy { it.title }.forEach {
            Button(modifier = Modifier.padding(10.dp),
                onClick = {
                    nav.navigate(NavRoute.SetTimer.create(it.id)) {
                        popUpTo(NavRoute.Home.route)
                    }
                }) {
                Text(text = it.title)
            }
        }
    }
}