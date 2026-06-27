package com.pab.scoutify.ui.dashboard

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.pab.scoutify.ui.dashboard.components.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    attendanceViewModel: AttendanceViewModel = hiltViewModel(),
    onNavigateToAttendance: (Long) -> Unit,
    onNavigateToActivities: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToManagement: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val attendanceState by attendanceViewModel.uiState.collectAsState()
    val userRole = uiState.userProfile?.role?.lowercase() ?: "siswa"
    val unreadCount = uiState.latestNotifications.count { !it.isRead }

    val context = LocalContext.current
    var showPermitDialog by remember { mutableStateOf(false) }
    var permitReason by remember { mutableStateOf("") }
    var permitType by remember { mutableStateOf("Izin") }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        hasLocationPermission = granted
        if (granted) {
            attendanceViewModel.loadInitialData()
            attendanceViewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

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

                        // 1b. Active Attendance Card (for students only)
                        if (userRole == "siswa" && attendanceState.activeActivity != null) {
                            item {
                                val active = attendanceState.activeActivity!!
                                val status = attendanceState.attendanceStatus ?: "Belum Presensi"
                                val isWithin = attendanceState.isWithinRadius

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "AGENDA HARI INI",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF5E35B1),
                                                letterSpacing = 1.sp
                                            )
                                            Surface(
                                                color = if (status.contains("Sudah") || status == "Hadir" || status.contains("Izin") || status.contains("Sakit")) Color(0xFFE8F5E9) else Color(0xFFFFF3CD),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = status.uppercase(),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (status.contains("Sudah") || status == "Hadir" || status.contains("Izin") || status.contains("Sakit")) Color(0xFF2E7D32) else Color(0xFF856404)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = active.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(active.locationName ?: "Tidak diketahui", color = Color.Gray, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        // Radius / Geofence Info
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = if (isWithin) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = if (isWithin) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isWithin) "Anda berada di dalam radius absensi (${attendanceState.distance.roundToInt()}m)" else "Anda di luar radius absensi (${attendanceState.distance.roundToInt()}m / target: ${active.radius}m)",
                                                fontSize = 12.sp,
                                                color = if (isWithin) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Action buttons (Only show if not checked in yet)
                                        val isCheckedIn = status.contains("Sudah") || status == "Hadir" || status.contains("Izin") || status.contains("Sakit")
                                        if (!isCheckedIn) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        if (isWithin) {
                                                            onNavigateToAttendance(active.id)
                                                        } else {
                                                            // Outside geofence, show leave pop-up immediately
                                                            showPermitDialog = true
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isWithin) Color(0xFF5E35B1) else Color(0xFFFFB300)
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = if (isWithin) Icons.Default.CameraAlt else Icons.Default.Info,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(if (isWithin) "Absen Selfie" else "Luar Radius")
                                                }

                                                OutlinedButton(
                                                    onClick = { showPermitDialog = true },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                                                ) {
                                                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Izin / Sakit", color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

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

    // Permit Modal Popup Dialog
    if (showPermitDialog) {
        AlertDialog(
            onDismissRequest = { showPermitDialog = false },
            title = { Text("Formulir Izin / Sakit", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Silakan pilih alasan ketidakhadiran Anda:")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        listOf("Izin", "Sakit", "Lainnya").forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = permitType == type,
                                    onClick = { permitType = type }
                                )
                                Text(type, modifier = Modifier.padding(start = 4.dp), fontSize = 14.sp)
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = permitReason,
                        onValueChange = { permitReason = it },
                        placeholder = { Text("Tuliskan alasan lengkap...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5E35B1)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (permitReason.isNotBlank()) {
                            attendanceViewModel.submitPermit(permitReason, permitType)
                            showPermitDialog = false
                            permitReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1))
                ) {
                    Text("Kirim Izin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermitDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
} // Ends Scaffold
} // Ends DashboardScreen
