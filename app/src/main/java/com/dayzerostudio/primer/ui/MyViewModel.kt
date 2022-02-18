package com.dayzerostudio.primer.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.dayzerostudio.primer.MyApplication

abstract class MyViewModel(context: Context, private val nav: NavHostController) : ViewModel() {
    internal val globals = (context as MyApplication).globals

    fun openSettings() {
        nav.navigate(NavRoute.Settings.create())
    }

    companion object {
        fun provideFactory(context: Context, nav: NavHostController):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return modelClass
                    .getConstructor(Context::class.java, NavHostController::class.java)
                    .newInstance(context, nav) as T
            }
        }
    }
}