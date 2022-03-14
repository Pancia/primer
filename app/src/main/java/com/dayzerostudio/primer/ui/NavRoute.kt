package com.dayzerostudio.primer.ui

import java.util.*

sealed class NavRoute(val route: String) {
    object FullScreenAlarm : NavRoute("fullscreen-alarm/{habitID}") {
        fun create(habitID: UUID) = "fullscreen-alarm/$habitID"
    }

    object Home : NavRoute("home") {
        fun create() = this.route
    }

    object Settings : NavRoute("settings") {
        fun create() = this.route
    }

    object ListOfHabits : NavRoute("habits-list") {
        fun create() = this.route
    }

    object HabitDetail : NavRoute("habit/{habitID}?isNew={isNew}") {
        fun create(habitID: UUID, isNew: Boolean = false) =
            "habit/$habitID?isNew=$isNew"
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