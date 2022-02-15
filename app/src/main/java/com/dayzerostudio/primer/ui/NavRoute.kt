package com.dayzerostudio.primer.ui

import java.util.*

sealed class NavRoute(val route: String) {
    object Home : NavRoute("home") {
        fun create() = this.route
    }

    object ListOfHabits : NavRoute("habits-list") {
        fun create() = this.route
    }

    object HabitDetail : NavRoute("habit/{habitID}") {
        fun create(habitID: UUID) = "habit/$habitID"
    }

    object SetTimer : NavRoute("set-timer/{habitID}?duration={duration}") {
        fun create(habitID: UUID, duration: Int? = null) =
            "set-timer/$habitID${if (duration != null) "?duration=$duration" else ""}"
    }

    object HabitRunning : NavRoute("habit-running/{habitID}/{duration}") {
        fun create(habitID: UUID, duration: Int) =
            "habit-running/$habitID/$duration"
    }
}