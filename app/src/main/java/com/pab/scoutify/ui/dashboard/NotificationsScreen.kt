package com.pab.scoutify.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pab.scoutify.model.NotificationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F2FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifikasi",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E35B1)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF5E35B1)
                        )
                    }
                },
                actions = {
                    if (uiState.notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Tandai Semua", color = Color(0xFF5E35B1), fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = { viewModel.loadNotifications() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF5E35B1))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF5E35B1)
                    )
                }
                uiState.notifications.isEmpty() -> {
                    EmptyNotificationsState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Unread count badge
                        val unreadCount = uiState.notifications.count { !it.isRead }
                        if (unreadCount > 0) {
                            item {
                                Surface(
                                    color = Color(0xFFEFEBFA),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Circle,
                                            contentDescription = null,
                                            tint = Color(0xFF5E35B1),
                                            modifier = Modifier.size(8.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "$unreadCount notifikasi belum dibaca",
                                            color = Color(0xFF5E35B1),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        items(uiState.notifications, key = { it.id }) { notification ->
                            RealNotificationCard(
                                notification = notification,
                                onMarkAsRead = { viewModel.markAsRead(notification.id.toLong()) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            // Error Snackbar
            uiState.errorMessage?.let { error ->
                LaunchedEffect(error) {
                    viewModel.clearError()
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFEBFA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = Color(0xFF5E35B1),
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Tidak ada notifikasi",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5E35B1),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Semua notifikasi dari pembina\nakan muncul di sini",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun RealNotificationCard(
    notification: NotificationItem,
    onMarkAsRead: () -> Unit
) {
    val categoryColor = when (notification.category?.uppercase()) {
        "ABSENSI" -> Color(0xFF5E35B1)
        "INFO" -> Color(0xFF1565C0)
        "UPDATE" -> Color(0xFFE65100)
        "KEGIATAN" -> Color(0xFF2E7D32)
        else -> Color(0xFF5E35B1)
    }

    val bgColor = if (!notification.isRead) Color.White else Color(0xFFFAFAFA)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!notification.isRead) onMarkAsRead() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (!notification.isRead) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.category?.uppercase()) {
                        "ABSENSI" -> Icons.Default.CheckCircle
                        "KEGIATAN" -> Icons.Default.Event
                        "INFO" -> Icons.Default.Info
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category chip
                    Surface(
                        color = categoryColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = notification.category ?: "INFO",
                            fontSize = 10.sp,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Unread indicator
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5E35B1))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.title,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A2E)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.content ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = notification.time ?: "",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}
