package com.example.myapplication.ui

sealed class NavRoute(val route: String) {
    object Home : NavRoute("home") {
        fun create() = this.route
    }

    object ListOfHabits : NavRoute("habits-list") {
        fun create() = this.route
    }

    object HabitDetail : NavRoute("habit/{habitName}") {
        fun create(habitName: String) = "habit/$habitName"
    }

    object PickHabit : NavRoute("habit-picker") {
        fun create() = this.route
    }

    object SetTimer : NavRoute("set-timer/{habitName}?duration={duration}") {
        fun create(habitName: String, duration: Int? = null) =
            "set-timer/$habitName${if (duration != null) "?duration=$duration" else ""}"
    }

    object HabitRunning : NavRoute("habit-running/{habitName}/{since}/{duration}") {
        fun create(habitName: String, now: Long, duration: Int) =
            "habit-running/$habitName/$now/$duration"
    }
}