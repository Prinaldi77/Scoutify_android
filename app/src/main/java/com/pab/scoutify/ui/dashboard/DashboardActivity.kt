package com.pab.scoutify.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pab.scoutify.ui.auth.LoginActivity
import com.pab.scoutify.ui.auth.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1B4332),
                    secondary = Color(0xFF2D6A4F),
                    tertiary = Color(0xFFFFD8B1)
                )
            ) {
                val navController = rememberNavController()
                val items = listOf(
                    BottomNavItem("Home", "home", Icons.Default.Home),
                    BottomNavItem("Activities", "activities", Icons.AutoMirrored.Filled.List),
                    BottomNavItem("Attendance", "attendance", Icons.AutoMirrored.Filled.Assignment),
                    BottomNavItem("Profile", "profile", Icons.Default.AccountCircle)
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        // Sembunyikan bottom bar jika berada di layar notifikasi atau detail lainnya jika perlu
                        val showBottomBar = items.any { it.route == currentDestination?.route }
                        
                        if (showBottomBar) {
                            NavigationBar(containerColor = Color.White) {
                                items.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF1B4332),
                                            selectedTextColor = Color(0xFF1B4332),
                                            indicatorColor = Color(0xFFFFD8B1).copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            DashboardScreen(
                                onNavigateToAttendance = { _ ->
                                    navController.navigate("attendance")
                                },
                                onNavigateToNotifications = {
                                    navController.navigate("notifications")
                                }
                            )
                        }
                        composable("activities") {
                            ActivitiesScreen(
                                onNavigateToDetail = { /* Navigate to activity detail */ },
                                onNotificationClick = {
                                    navController.navigate("notifications")
                                }
                            )
                        }
                        composable("attendance") {
                            AttendanceScreen(
                                onNavigateToNotifications = {
                                    navController.navigate("notifications")
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onEditProfile = { /* Navigate to edit profile */ },
                                onChangePassword = { /* Navigate to change password */ },
                                onLogoutSuccess = {
                                    val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            )
                        }
                        composable("notifications") {
                            NotificationsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
