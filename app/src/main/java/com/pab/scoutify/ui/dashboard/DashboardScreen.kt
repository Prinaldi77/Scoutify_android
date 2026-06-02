package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pab.scoutify.ui.dashboard.components.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAttendance: (Long) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            DashboardTopAppBar(onNotificationClick = onNavigateToNotifications)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.errorMessage ?: "Terjadi kesalahan")
                    Button(onClick = { viewModel.loadDashboardData() }) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item { HeaderUserSection(uiState.userProfile) }

                    item {
                        Text(
                            text = "Ringkasan Statistik",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        uiState.summary?.let { summary ->
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    StatisticCard(
                                        title = "Total Members",
                                        value = summary.totalMembers.toString(),
                                        icon = Icons.Default.Groups,
                                        containerColor = Color(0xFFF8F9FA),
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatisticCard(
                                        title = "Total Activities",
                                        value = summary.totalActivities.toString(),
                                        icon = Icons.Default.CalendarToday,
                                        containerColor = Color(0xFFFFD8B1),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    StatisticCard(
                                        title = "Today Attendance",
                                        value = summary.todayAttendance.toString(),
                                        icon = Icons.Default.Person,
                                        containerColor = Color(0xFF2D6A4F),
                                        contentColor = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatisticCard(
                                        title = "Trainers",
                                        value = summary.totalTrainers.toString(),
                                        icon = Icons.Default.EmojiEvents,
                                        containerColor = Color(0xFFE9ECEF),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(
                            title = "Aktivitas Mendatang",
                            onSeeAllClick = { /* Navigate to activities */ }
                        )
                    }

                    items(uiState.upcomingActivities) { activity ->
                        UpcomingActivityCard(
                            activity = activity,
                            onCheckInClick = { onNavigateToAttendance(activity.id) }
                        )
                    }

                    item {
                        SectionHeader(
                            title = "Notifikasi Terbaru",
                            onSeeAllClick = onNavigateToNotifications
                        )
                    }

                    items(uiState.latestNotifications) { notification ->
                        NotificationCard(notification)
                    }
                }
            }
        }
    }
}
