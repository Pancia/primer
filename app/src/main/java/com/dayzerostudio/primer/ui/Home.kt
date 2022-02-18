package com.dayzerostudio.primer.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dayzerostudio.primer.R
import com.dayzerostudio.primer.sendZipToServer
import java.net.ConnectException
import kotlin.concurrent.thread

enum class HomeTab {
    HOME(),
    HABITS()
}

class HomeViewModel(val context: Context, val nav: NavHostController) : MyViewModel(context, nav) {
    fun exportHabits() {
        val toast = Toast.makeText(context, "Failed to connect to server", Toast.LENGTH_LONG)
        thread {
            val z = globals.storage.createZip()
            try {
                sendZipToServer(z)
            } catch (e: ConnectException) {
                Log.e("export-habits", "Failed to export habits!", e)
                toast.show()
            }
        }
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
        nav.navigate(NavRoute.HabitDetail.create(habit.id))
    }

    fun getGlobalText() = globals.storage.getGlobalText()
    fun setGlobalText(text: String) = globals.storage.editGlobalText(text)
}

@Composable
fun HomeTab(vm: HomeViewModel, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(padding)
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
        DebouncedTextField(
            initialValue = vm.getGlobalText(),
            debouncedOnValueChange = { vm.setGlobalText(it) },
            scope = vm.viewModelScope,
            modifier = Modifier
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
            TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
                IconButton(onClick = { showOverflow = !showOverflow }) {
                    Icon(Icons.Default.MoreVert, "More")
                }
                DropdownMenu(expanded = showOverflow,
                    onDismissRequest = { showOverflow = false }) {
                    DropdownMenuItem(onClick = { vm.exportHabits() }) {
                        Icon(Icons.Default.Share, "Export")
                        Text("Export")
                    }
                    DropdownMenuItem(onClick = { vm.openSettings() }) {
                        Icon(Icons.Default.Settings, "Settings")
                        Text("Settings")
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
                    Icon(Icons.Default.Add, "Create a new Habit")
                }
            }
        }
    ) {
        when (tab) {
            HomeTab.HOME -> HomeTab(vm, it)
            HomeTab.HABITS -> {
                val newVM: HabitsListViewModel =
                    viewModel(factory = MyViewModel.provideFactory(vm.context, vm.nav))
                HabitsTab(newVM, it)
            }
        }
    }
}
