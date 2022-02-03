package com.example.myapplication.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.Habit

class HabitsListViewModel(context: Context) : ViewModel() {
    val habits = mutableStateListOf<Habit>()
    private val spList = context.getSharedPreferences("habitsList", 0)!!

    init {
        habits.addAll(spList.all.values.toList().map { Habit(it as String) })
    }

    fun createHabit(title: String): Habit {
        val habit = Habit(title = title)
        habits.add(habit)
        spList.edit().putString(title, title).apply()
        return habit
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