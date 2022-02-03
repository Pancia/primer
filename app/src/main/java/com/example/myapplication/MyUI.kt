package com.example.myapplication

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.*

@Composable
fun MyApp(nav: NavHostController, context: Context) {
    NavHost(navController = nav, startDestination = NavRoute.Home.create()) {
        composable(NavRoute.Home.route) { Home(nav) }
        composable(NavRoute.ListOfHabits.route) {
            val habitsListViewModel: HabitsListViewModel =
                viewModel(factory = HabitsListViewModel.provideFactory(context))
            HabitsList(nav, habitsListViewModel)
        }
        composable(NavRoute.HabitDetail.route) {
            val habitDetailViewModel: HabitDetailViewModel =
                viewModel(factory = HabitDetailViewModel.provideFactory(context))
            HabitDetail(
                nav,
                habitDetailViewModel,
                it.arguments?.getString("habitID")!!
            )
        }
        composable(NavRoute.PickHabit.route) {
            val habitsListViewModel: HabitsListViewModel =
                viewModel(factory = HabitsListViewModel.provideFactory(context))
            HabitPicker(nav, habitsListViewModel)
        }
        composable(
            NavRoute.SetTimer.route,
            arguments = listOf(
                navArgument("habitID") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType; defaultValue = 0 },
            )
        ) {
            val habitID = it.arguments?.getString("habitID")!!
            val timerViewModel: TimerViewModel =
                viewModel(
                    factory = TimerViewModel.provideFactory(
                        context,
                        nav,
                        habitID,
                        it.arguments?.getInt("duration")
                    )
                )
            HabitTimer(timerViewModel, habitID)
        }
        composable(
            NavRoute.HabitRunning.route,
            arguments = listOf(
                navArgument("habitID") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) {
            val runningViewModel: RunningViewModel =
                viewModel(factory = RunningViewModel.provideFactory(context, nav))
            val duration = it.arguments?.getInt("duration")!!
            runningViewModel.startCountdown(duration)
            HabitRunning(
                nav,
                runningViewModel,
                it.arguments?.getString("habitID")!!,
                duration
            )
        }
    }
}
