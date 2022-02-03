package com.example.myapplication

// TODO notes should allow for images
data class Note(val text: String)

data class Habit(
    val title: String,
    val description: String = "Default description",
    val notes: List<Note> = emptyList()
)