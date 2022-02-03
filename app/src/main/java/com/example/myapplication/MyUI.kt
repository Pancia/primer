package com.example.myapplication

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.*

@Composable
fun MyApp(navController: NavHostController, context: Context) {
    NavHost(navController = navController, startDestination = NavRoute.Home.create()) {
        composable(NavRoute.Home.route) { Home(navController) }
        composable(NavRoute.ListOfHabits.route) {
            val habitsListViewModel: HabitsListViewModel =
                viewModel(factory = HabitsListViewModel.provideFactory(context))
            HabitsList(navController, habitsListViewModel)
        }
        composable(NavRoute.HabitDetail.route) {
            HabitDetail(
                navController,
                it.arguments?.getString("habitName")!!
            )
        }
        composable(NavRoute.PickHabit.route) {
            val habitsListViewModel: HabitsListViewModel =
                viewModel(factory = HabitsListViewModel.provideFactory(context))
            HabitPicker(navController, habitsListViewModel)
        }
        composable(
            NavRoute.SetTimer.route,
            arguments = listOf(
                navArgument("habitName") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType; defaultValue = 0 },
            )
        ) {
            val habitName = it.arguments?.getString("habitName")!!
            val timerViewModel: TimerViewModel =
                viewModel(factory = TimerViewModel.provideFactory(context, habitName, it.arguments?.getInt("duration")))
            HabitTimer(
                navController,
                timerViewModel,
                habitName
            )
        }
        composable(
            NavRoute.HabitRunning.route,
            arguments = listOf(
                navArgument("habitName") { type = NavType.StringType },
                navArgument("since") { type = NavType.LongType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) {
            val runningViewModel: RunningViewModel =
                viewModel(RunningViewModel::class.java)
            val duration = it.arguments?.getInt("duration")!!
            runningViewModel.startCountdown(duration)
            HabitRunning(
                navController,
                runningViewModel,
                it.arguments?.getString("habitName")!!,
                it.arguments?.getLong("since")!!,
                duration
            )
        }
    }
}
