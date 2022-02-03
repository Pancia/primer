package com.example.myapplication.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun HabitDetail(navController: NavHostController, habitName: String) {
    Column {
        Text(text = habitName)
        Text(text = "Description")
        Text(text = "TODO")
        val notes = listOf("note-1", "note-2")
        notes.forEach {
            Text(text = it)
        }
    }
}