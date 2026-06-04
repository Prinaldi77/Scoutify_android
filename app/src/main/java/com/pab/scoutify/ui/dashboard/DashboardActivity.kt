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
                    primary = Color(0xFF5E35B1),      // Royal Purple
                    secondary = Color(0xFF9C27B0),    // Lilac/Orchid Accent
                    tertiary = Color(0xFFFFB300),     // Warm Gold
                    background = Color(0xFFF5F2FA),   // Soft Lavender Cream background
                    surface = Color(0xFFFBF9FF),      // Lavender Tint white
                    onPrimary = Color.White,
                    onSecondary = Color.White
                )
            ) {
                val navController = rememberNavController()
                val userRole = sessionManager.getRole().lowercase()

                // Role-based Menu Items
                val items = if (userRole == "pembina" || userRole == "admin") {
                    listOf(
                        BottomNavItem("Beranda", "home", Icons.Default.Home),
                        BottomNavItem("Kegiatan", "activities", Icons.AutoMirrored.Filled.List),
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
                                            selectedIconColor = Color(0xFF5E35B1),
                                            indicatorColor = Color(0xFFEFEBFA)
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
                                    if (userRole == "pembina" || userRole == "admin") {
                                        navController.navigate("reports")
                                    } else {
                                        navController.navigate("attendance")
                                    }
                                },
                                onNavigateToActivities = { navController.navigate("activities") },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToManagement = { navController.navigate("management") }
                            )
                        }
                        composable("activities") {
                            if (userRole == "pembina" || userRole == "admin") {
                                ActivityManagementScreen(
                                    onMenuClick = { },
                                    onSearchClick = { },
                                    onAddActivityClick = { navController.navigate("add_activity") },
                                    onEditActivity = { activityId -> navController.navigate("edit_activity/$activityId") },
                                    onCheckDetail = { navController.navigate("reports") }
                                )
                            } else {
                                ActivitiesScreen(
                                    onNavigateToDetail = { },
                                    onNotificationClick = { navController.navigate("notifications") }
                                )
                            }
                        }
                        composable("add_activity") {
                            AddActivityScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("edit_activity/{activityId}") {
                            AddActivityScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("attendance") {
                            AttendanceScreen(
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToSelfie = { activityId, attendanceId, lat, lng ->
                                    navController.navigate("selfie/$activityId/$attendanceId/$lat/$lng")
                                }
                            )
                        }
                        composable("selfie/{activityId}/{attendanceId}/{lat}/{lng}") { backStackEntry ->
                            val activityId = backStackEntry.arguments?.getString("activityId")?.toLongOrNull() ?: 0L
                            val attendanceId = backStackEntry.arguments?.getString("attendanceId")?.toLongOrNull() ?: 0L
                            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: -6.9726
                            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 107.5908
                            SelfieVerificationScreen(
                                activityId = activityId,
                                attendanceId = attendanceId,
                                latitude = lat,
                                longitude = lng,
                                onNavigateBack = { navController.popBackStack() },
                                onSuccess = {
                                    navController.navigate("attendance") {
                                        popUpTo("attendance") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("management") {
                            ManagementScreen(
                                onMenuClick = { /* Optional */ },
                                onSearchClick = { /* Optional */ },
                                onAddMemberClick = { navController.navigate("add_member") },
                                onMemberClick = { memberId -> navController.navigate("member_detail/$memberId") }
                            )
                        }
                        composable("add_member") {
                            AddMemberScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("member_detail/{memberId}") {
                            MemberDetailScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onEditData = { memberId -> navController.navigate("edit_member/$memberId") }
                            )
                        }
                        composable("edit_member/{memberId}") {
                            AddMemberScreen(
                                onNavigateBack = { navController.popBackStack() }
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
