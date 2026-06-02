package com.pab.scoutify.ui.dashboard

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.pab.scoutify.model.ActiveActivity
import com.pab.scoutify.model.AttendanceUiState
import com.pab.scoutify.model.UserLocation
import com.pab.scoutify.ui.dashboard.components.DashboardTopAppBar

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) {
            // Handle permission denied
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

    LaunchedEffect(uiState.checkInSuccess) {
        if (uiState.checkInSuccess) {
            snackbarHostState.showSnackbar("Check-in berhasil!")
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            DashboardTopAppBar(onNotificationClick = onNavigateToNotifications)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.activeActivity == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActiveActivityCard(uiState.activeActivity)

                    MapSection(
                        activeActivity = uiState.activeActivity,
                        userLocation = uiState.userLocation,
                        radius = uiState.activeActivity?.radius ?: 0
                    )

                    RadiusStatusSection(
                        isWithinRadius = uiState.isWithinRadius,
                        distance = uiState.distance
                    )

                    Button(
                        onClick = { viewModel.checkIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B4332),
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = uiState.isWithinRadius && !uiState.isLoading && uiState.attendanceStatus != "Sudah Check In"
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonPin, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (uiState.attendanceStatus == "Sudah Check In") "Sudah Check In" else "Check In Sekarang",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = uiState.activeActivity?.timeRange ?: "--:-- WIB",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "12 Oct 2023", // Data statis atau dari API
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveActivityCard(activity: ActiveActivity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1B4332), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "AKTIVITAS BERLANGSUNG",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B4513)
                )
                Text(
                    text = activity?.name ?: "Tidak ada kegiatan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = activity?.locationName ?: "Lokasi tidak diketahui",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MapSection(
    activeActivity: ActiveActivity?,
    userLocation: UserLocation?,
    radius: Int
) {
    val targetLatLng = activeActivity?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(0.0, 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLatLng, 17f)
    }

    LaunchedEffect(targetLatLng) {
        if (targetLatLng.latitude != 0.0) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(targetLatLng, 17f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = userLocation != null),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            if (activeActivity != null) {
                Marker(
                    state = MarkerState(position = targetLatLng),
                    title = activeActivity.name
                )
                Circle(
                    center = targetLatLng,
                    radius = radius.toDouble(),
                    fillColor = Color(0x442D6A4F),
                    strokeColor = Color(0xFF2D6A4F),
                    strokeWidth = 2f
                )
            }
        }

        // GPS Info Overlay
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Koordinat Anda", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "${userLocation?.latitude ?: 0.0}, ${userLocation?.longitude ?: 0.0}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Akurasi", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "± ${userLocation?.accuracy?.toInt() ?: 0} meter",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RadiusStatusSection(isWithinRadius: Boolean, distance: Double) {
    val backgroundColor = if (isWithinRadius) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val contentColor = if (isWithinRadius) Color(0xFF2E7D32) else Color(0xFFC62828)
    val icon = if (isWithinRadius) Icons.Default.CheckCircle else Icons.Default.Cancel

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isWithinRadius) "Anda berada dalam radius" else "Anda berada di luar radius",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = if (isWithinRadius) "Lokasi valid. Silakan lakukan Check In." else "Jarak Anda: ${distance.toInt()}m. Silakan mendekat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
