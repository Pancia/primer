package com.dayzerostudio.primer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dayzerostudio.primer.ui.*

@Composable
fun MyApp(nav: NavHostController, application: MyApplication) {
    val context = application as Context
    NavHost(navController = nav, startDestination = NavRoute.Home.create()) {
        composable(NavRoute.FullScreenAlarm.route) {
            val habitID = it.arguments?.getString("habitID")!!
            val vm: AlarmViewModel =
                viewModel(factory = MyViewModel.provideFactory(context, nav))
            FullScreenAlarm(vm, habitID)
        }
        composable(NavRoute.Home.route) {
            val vm: HomeViewModel =
                viewModel(factory = MyViewModel.provideFactory(context, nav))
            Home(vm, HomeTab.HOME)
        }
        composable(NavRoute.Settings.route) {
            val vm: SettingsViewModel =
                viewModel(factory = MyViewModel.provideFactory(context, nav))
            Settings(vm)
        }
        composable(NavRoute.ListOfHabits.route) {
            val vm: HomeViewModel =
                viewModel(factory = MyViewModel.provideFactory(context, nav))
            Home(vm, HomeTab.HABITS)
        }
        composable(NavRoute.HabitDetail.route) {
            val vm: HabitDetailViewModel =
                viewModel(factory = MyViewModel.provideFactory(context, nav))
            val habitID = it.arguments?.getString("habitID")!!
            HabitDetail(vm, habitID)
        }
        composable(
            NavRoute.SetTimer.route,
            arguments = listOf(
                navArgument("habitID") { type = NavType.StringType },
                navArgument("duration") { type = NavType.StringType; defaultValue = "" },
            )
        ) {
            val habitID = it.arguments?.getString("habitID")!!
            val duration = it.arguments?.getString("duration")
            val vm: TimerViewModel =
                viewModel(factory = MyViewModel.provideFactory(context, nav))
            vm.init(habitID, duration)
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
                viewModel(factory = MyViewModel.provideFactory(context, nav))
            vm.startCountdown(duration)
            HabitRunning(vm, habitID, duration)
        }
    }
}
