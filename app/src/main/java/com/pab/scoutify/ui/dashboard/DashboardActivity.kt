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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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

                // Student-only Menu Items
                val items = listOf(
                    BottomNavItem("Beranda", "home", Icons.Default.Home),
                    BottomNavItem("Profil", "profile", Icons.Default.Person),
                    BottomNavItem("Kalender", "kalender", Icons.Default.DateRange),
                    BottomNavItem("Lainnya", "menu_lainnya", Icons.Default.Menu)
                )

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
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { fadeIn(animationSpec = tween(200)) },
                        exitTransition = { fadeOut(animationSpec = tween(200)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
                        popExitTransition = { fadeOut(animationSpec = tween(200)) }
                    ) {
                        composable("home") {
                            DashboardScreen(
                                onNavigateToAttendance = { activityId ->
                                    // Direct check-in to Selfie Verification screen
                                    navController.navigate("selfie/$activityId/0/-7.0278/107.5756")
                                },
                                onNavigateToActivities = { navController.navigate("kalender") },
                                onNavigateToNotifications = { navController.navigate("notifications") }
                            )
                        }
                        // Anggota route removed (replaced by profile tab)
                        composable("kalender") {
                            KalenderScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("menu_lainnya") {
                            MenuLainnyaScreen(
                                onNavigateToProfile = { navController.navigate("profile") },
                                onLogoutSuccess = {
                                    sessionManager.clearSession()
                                    val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            )
                        }
                        composable("selfie/{activityId}/{attendanceId}/{lat}/{lng}") { backStackEntry ->
                            val activityId = backStackEntry.arguments?.getString("activityId")?.toLongOrNull() ?: 0L
                            val attendanceId = backStackEntry.arguments?.getString("attendanceId")?.toLongOrNull() ?: 0L
                            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: -7.0278
                            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 107.5756
                            SelfieVerificationScreen(
                                activityId = activityId,
                                attendanceId = attendanceId,
                                latitude = lat,
                                longitude = lng,
                                onNavigateBack = { navController.popBackStack() },
                                onSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigateBack = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onEditProfile = { },
                                onChangePassword = { },
                                onSettingsClick = {
                                    val intent = Intent(this@DashboardActivity, SettingsActivity::class.java)
                                    startActivity(intent)
                                },
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
