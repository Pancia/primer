package com.example.myapplication

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    lateinit var nav: NavHostController

    private fun navigateTo(route: String?) {
        route?.let {
            (application as MyApplication).stopAlarm()
            (this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(0)
            nav.navigate(it)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val route = intent?.extras?.getString("navigateTo")
        Log.e("DBG", "onNewIntent: $route")
        navigateTo(route)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val route = intent?.extras?.getString("navigateTo")
        Log.e("DBG", "navTo: $route")
        setContent {
            MyApplicationTheme {
                nav = rememberNavController()
                MyApp(nav, application)
                navigateTo(route)
            }
        }
    }
}