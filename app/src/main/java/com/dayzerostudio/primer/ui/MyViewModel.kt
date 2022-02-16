package com.dayzerostudio.primer.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController

abstract class MyViewModel() : ViewModel() {
    companion object {
        fun provideFactory(context: Context, nav: NavHostController):
                ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return modelClass
                    .getConstructor(Context::class.java, NavHostController::class.java)
                    .newInstance(context, nav) as T
            }
        }
    }
}