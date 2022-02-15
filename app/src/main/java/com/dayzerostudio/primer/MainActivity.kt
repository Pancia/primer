package com.dayzerostudio.primer

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dayzerostudio.primer.ui.theme.MyApplicationTheme

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
        navigateTo(route)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val route = intent?.extras?.getString("navigateTo")
        setContent {
            MyApplicationTheme {
                nav = rememberNavController()
                MyApp(nav, application as MyApplication)
                navigateTo(route)
            }
        }
    }
}