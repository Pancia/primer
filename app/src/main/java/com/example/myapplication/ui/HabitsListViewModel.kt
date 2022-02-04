package com.example.myapplication.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.example.myapplication.Habit
import com.example.myapplication.MyApplication

class HabitsListViewModel(
    context: Context,
    private val nav: NavHostController
) : ViewModel() {
    val habits = mutableStateListOf<Habit>()
    private val storage = (context as MyApplication).globals.storage

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

    fun navToHabit(habit: Habit) {
        nav.navigate(NavRoute.HabitDetail.create(habit.id))
    }

    fun navToTimer(habit: Habit) {
        nav.navigate(NavRoute.SetTimer.create(habit.id))
    }

    companion object {
        fun provideFactory(context: Context, nav: NavHostController):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HabitsListViewModel(context, nav) as T
            }
        }
    }
}