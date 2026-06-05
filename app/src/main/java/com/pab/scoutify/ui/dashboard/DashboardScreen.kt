package com.pab.scoutify.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pab.scoutify.ui.dashboard.components.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAttendance: (Long) -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToManagement: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val userRole = uiState.userProfile?.role?.lowercase() ?: "siswa"
    val unreadCount = uiState.latestNotifications.count { !it.isRead }

    Scaffold(
        topBar = {
            DashboardTopAppBar(
                unreadCount = unreadCount,
                onNotificationClick = onNavigateToNotifications
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F2FA))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF5E35B1))
            } else {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                        initialOffsetY = { 120 },
                        animationSpec = tween(600)
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        // 1. Header Profil Mewah
                        item { HeaderUserSection(uiState.userProfile) }

                        // 2. Konten Spesifik berdasarkan Role (PEMBINA vs SISWA)
                        if (userRole == "pembina" || userRole == "admin") {
                            item {
                                SectionHeader(title = "Statistik Gudep", onSeeAllClick = onNavigateToManagement)
                            }
                            item {
                                uiState.summary?.let { summary ->
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        // Total Anggota - Hero Card (Full Width, Taller)
                                        StatisticCard(
                                            title = "Total Anggota Gudep",
                                            value = summary.totalMembers.toString(),
                                            icon = Icons.Default.Groups,
                                            containerColor = Color(0xFFEFEBFA), // soft purple tint
                                            contentColor = Color(0xFF5E35B1),
                                            onClick = onNavigateToManagement,
                                            modifier = Modifier.fillMaxWidth(),
                                            height = 135.dp
                                        )
                                        
                                        // Row for two side-by-side cards (Kegiatan and Hadir Hari Ini)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            StatisticCard(
                                                title = "Kegiatan",
                                                value = summary.totalActivities.toString(),
                                                icon = Icons.Default.Map,
                                                containerColor = Color(0xFFFFF3CD),
                                                contentColor = Color(0xFF856404),
                                                onClick = onNavigateToActivities,
                                                modifier = Modifier.weight(1f),
                                                height = 115.dp
                                            )
                                            StatisticCard(
                                                title = "Hadir Hari Ini",
                                                value = summary.todayAttendance.toString(),
                                                icon = Icons.Default.CheckCircle,
                                                containerColor = Color(0xFFE3F2FD),
                                                contentColor = Color(0xFF1565C0),
                                                onClick = { /* Detail Kehadiran */ },
                                                modifier = Modifier.weight(1f),
                                                height = 115.dp
                                            )
                                        }

                                        // Staf Pembina - Horizontal Bottom Card (Full Width, Shorter)
                                        StatisticCard(
                                            title = "Staf Pembina / Pembina Pendamping",
                                            value = summary.totalTrainers.toString(),
                                            icon = Icons.Default.Badge,
                                            containerColor = Color(0xFFFCE4EC),
                                            contentColor = Color(0xFFC2185B),
                                            onClick = { /* Daftar Pembina */ },
                                            modifier = Modifier.fillMaxWidth(),
                                            height = 95.dp
                                        )
                                    }
                                }
                            }
                        } else {
                        // Tampilan Dashboard untuk SISWA
                        item {
                            SectionHeader(title = "Capaian Saya", onSeeAllClick = { onNavigateToAttendance(0) })
                        }
                        item {
                            StatisticCard(
                                title = "Kehadiran",
                                value = "${uiState.summary?.todayAttendance ?: 0}%",
                                icon = Icons.Default.Timeline,
                                containerColor = Color(0xFF5E35B1),
                                contentColor = Color.White,
                                onClick = { onNavigateToAttendance(0) },
                                modifier = Modifier.fillMaxWidth(),
                                height = 125.dp
                            )
                        }
                    }

                    // 3. Agenda Mendatang (Dinamis)
                    item {
                        SectionHeader(title = "Agenda Pramuka", onSeeAllClick = onNavigateToActivities)
                    }

                    if (uiState.upcomingActivities.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    "Belum ada agenda terdekat.",
                                    modifier = Modifier.padding(24.dp),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(uiState.upcomingActivities) { activity ->
                            UpcomingActivityCard(
                                activity = activity,
                                onCheckInClick = { onNavigateToAttendance(activity.id) }
                            )
                        }
                    }

                    // 4. Pesan & Info
                    item {
                        SectionHeader(title = "Info Terbaru", onSeeAllClick = onNavigateToNotifications)
                    }

                    items(uiState.latestNotifications.take(3)) { notification ->
                        NotificationCard(notification, onClick = onNavigateToNotifications)
                    } // Ends items
                } // Ends LazyColumn
            } // Ends AnimatedVisibility
        } // Ends else
    } // Ends Box
} // Ends Scaffold
} // Ends DashboardScreen
