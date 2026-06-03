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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
                val userRole = sessionManager.getRole().lowercase()

                // Role-based Menu Items
                val items = if (userRole == "pembina" || userRole == "admin") {
                    listOf(
                        BottomNavItem("Beranda", "home", Icons.Default.Home),
                        BottomNavItem("Anggota", "management", Icons.Default.Groups),
                        BottomNavItem("Laporan", "reports", Icons.Default.Assessment),
                        BottomNavItem("Profil", "profile", Icons.Default.Person)
                    )
                } else {
                    listOf(
                        BottomNavItem("Beranda", "home", Icons.Default.Home),
                        BottomNavItem("Kegiatan", "activities", Icons.AutoMirrored.Filled.List),
                        BottomNavItem("Presensi", "attendance", Icons.AutoMirrored.Filled.Assignment),
                        BottomNavItem("Profil", "profile", Icons.Default.Person)
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        val showBottomBar = items.any { it.route == currentDestination?.route }

                        if (showBottomBar) {
                            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
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
                                onNavigateToAttendance = { _ -> navController.navigate("attendance") },
                                onNavigateToActivities = { navController.navigate("activities") },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToManagement = { navController.navigate("management") }
                            )
                        }
                        composable("activities") {
                            ActivitiesScreen(
                                onNavigateToDetail = { },
                                onNotificationClick = { navController.navigate("notifications") }
                            )
                        }
                        composable("attendance") {
                            AttendanceScreen(
                                onNavigateToNotifications = { navController.navigate("notifications") }
                            )
                        }
                        composable("management") {
                            ManagementScreen(
                                onMenuClick = { /* Optional */ },
                                onSearchClick = { /* Optional */ },
                                onAddMemberClick = { /* Navigate to add member */ }
                            )
                        }
                        composable("reports") {
                            AttendanceReportScreen(
                                onMenuClick = { /* Optional */ },
                                onSearchClick = { /* Optional */ },
                                onProfileClick = { navController.navigate("profile") }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onEditProfile = { },
                                onChangePassword = { },
                                onLogoutSuccess = {
                                    sessionManager.clearSession()
                                    val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            )
                        }
                        composable("notifications") {
                            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
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
