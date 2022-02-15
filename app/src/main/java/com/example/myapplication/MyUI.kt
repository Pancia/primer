package com.example.myapplication

import android.content.Context
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.*

@Composable
fun MyApp(nav: NavHostController, application: MyApplication) {
    val context = application as Context
    NavHost(navController = nav, startDestination = NavRoute.Home.create()) {
        composable(NavRoute.Home.route) {
            val vm: HomeViewModel =
                viewModel(factory = HomeViewModel.provideFactory(context, nav))
            Home(vm, HomeTab.HOME)
        }
        composable(NavRoute.ListOfHabits.route) {
            val vm: HomeViewModel =
                viewModel(factory = HomeViewModel.provideFactory(context, nav))
            Home(vm, HomeTab.HABITS)
        }
        composable(NavRoute.HabitDetail.route) {
            val vm: HabitDetailViewModel =
                viewModel(factory = HabitDetailViewModel.provideFactory(context, nav))
            val habitID = it.arguments?.getString("habitID")!!
            HabitDetail(vm, habitID)
        }
        composable(
            NavRoute.SetTimer.route,
            arguments = listOf(
                navArgument("habitID") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType; defaultValue = 0 },
            )
        ) {
            val habitID = it.arguments?.getString("habitID")!!
            val duration = it.arguments?.getInt("duration")
            val vm: TimerViewModel =
                viewModel(
                    factory = TimerViewModel.provideFactory(
                        context,
                        nav,
                        habitID,
                        duration
                    )
                )
            HabitTimer(vm, habitID)
        }
        composable(
            NavRoute.HabitRunning.route,
            arguments = listOf(
                navArgument("habitID") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) {
            val habitID = it.arguments?.getString("habitID")!!
            val duration = it.arguments?.getInt("duration")!!
            val vm: RunningViewModel =
                viewModel(factory = RunningViewModel.provideFactory(context, nav))
            vm.startCountdown(duration)
            HabitRunning(vm, habitID, duration)
        }
    }
}
