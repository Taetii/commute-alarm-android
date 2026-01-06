package com.taehyeong.commutealarm.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.taehyeong.commutealarm.R
import com.taehyeong.commutealarm.ui.screens.HomeScreen
import com.taehyeong.commutealarm.ui.screens.LeaveScreen
import com.taehyeong.commutealarm.ui.screens.HelpScreen

sealed class Screen(val route: String, val labelRes: Int, val icon: @Composable () -> Unit) {
    data object Home : Screen("home", R.string.nav_home, { Icon(Icons.Filled.Home, contentDescription = null) })
    data object Leave : Screen("leave", R.string.nav_leave, { Icon(Icons.Filled.DateRange, contentDescription = null) })
    data object Help : Screen("help", R.string.nav_help, { Icon(Icons.Filled.Info, contentDescription = null) })
}

@Composable
fun CommuteAlarmApp() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Leave, Screen.Help)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(stringResource(screen.labelRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Leave.route) { LeaveScreen() }
            composable(Screen.Help.route) { HelpScreen() }
        }
    }
}
