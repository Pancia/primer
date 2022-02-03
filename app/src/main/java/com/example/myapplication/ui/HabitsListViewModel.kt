package com.example.myapplication.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.Habit

class HabitsListViewModel(context: Context) : ViewModel() {
    val habits = mutableStateListOf<Habit>()
    private val storage = HabitStorage(context)

    init {
        habits.addAll(storage.getAllTitles())
    }

    fun createHabit(title: String): Habit {
        val habit = storage.create(title)
        habits.add(habit)
        return habit
    }

    fun deleteAllHabits() {
        habits.clear()
        storage.deleteAll()
    }

    companion object {
        fun provideFactory(context: Context):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HabitsListViewModel(context) as T
            }
        }
    }
}