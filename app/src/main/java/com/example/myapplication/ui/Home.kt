package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.MyApplication
import com.example.myapplication.sendZipToServer
import kotlin.concurrent.thread

enum class HomeTab {
    HOME(),
    HABITS()
}

class HomeViewModel(val context: Context, val nav: NavHostController) : ViewModel() {
    private val globals = (context as MyApplication).globals

    fun exportHabits() =
        thread {
            val z = globals.storage.createZip()
            sendZipToServer(z)
        }

    fun gotoHabits() {
        nav.navigate(NavRoute.ListOfHabits.create())
    }

    fun gotoHome() {
        nav.navigate(NavRoute.Home.create())
    }

    val activeHabitID = globals.timeKeeper.activeHabitID

    fun gotoRunningHabit() {
        nav.navigate(
            NavRoute.HabitRunning.create(
                globals.timeKeeper.activeHabitID.value!!,
                globals.timeKeeper.timeLeft()
            )
        )
    }

    fun getHabitInfoByID(s: String) = globals.storage.getHabitInfoByID(s)
    fun navToNewHabit() {
        val habit = globals.storage.create("TempTitle")
        //habits.add(habit)
        nav.navigate(NavRoute.HabitDetail.create(habit.id))
    }

    fun getGlobalText() = globals.storage.getGlobalText()
    fun setGlobalText(text: String) = globals.storage.editGlobalText(text)

    companion object {
        fun provideFactory(context: Context, nav: NavHostController):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(context, nav) as T
            }
        }
    }
}

@Composable
fun HomeTab(vm: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val activeID = vm.activeHabitID.value
        val activeHabit = vm.getHabitInfoByID("$activeID")
        if (vm.activeHabitID.value != null && activeHabit != null) {
            Button(onClick = {
                vm.gotoRunningHabit()
            }) {
                Text(text = "GOTO: ${activeHabit.title}")
            }
        }
        val globalText = remember { mutableStateOf(vm.getGlobalText()) }
        TextField(
            value = globalText.value,
            onValueChange = { globalText.value = it },
            //label = { Text("Title") },
            textStyle = MaterialTheme.typography.h5,
            modifier = Modifier
                .onFocusChanged {
                    vm.setGlobalText(globalText.value)
                }
                .fillMaxWidth(1f)
                .weight(1f)
        )
    }
}

@Composable
fun Home(vm: HomeViewModel, tab: HomeTab) {
    var showOverflow by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MyApp") }, actions = {
                IconButton(onClick = { showOverflow = !showOverflow }) {
                    Icon(Icons.Default.MoreVert, "More")
                }
                DropdownMenu(expanded = showOverflow,
                    onDismissRequest = { showOverflow = false }) {
                    DropdownMenuItem(onClick = { vm.exportHabits() }) {
                        Icon(Icons.Default.Share, "Export")
                        Text("Export")
                    }
                }
            })
        },
        bottomBar = {
            BottomAppBar() {
                IconButton(onClick = { vm.gotoHome() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Home, "Home")
                }
                IconButton(onClick = { vm.gotoHabits() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.List, "View Habits")
                }
                IconButton(onClick = { vm.navToNewHabit() }, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create a new Habit")
                }
            }
        }
    ) {
        when (tab) {
            HomeTab.HOME -> HomeTab(vm)
            HomeTab.HABITS -> {
                val newVM: HabitsListViewModel =
                    viewModel(factory = HabitsListViewModel.provideFactory(vm.context, vm.nav))
                HabitsTab(newVM)
            }
        }
    }
}
