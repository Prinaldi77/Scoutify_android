package com.pab.scoutify.ui.dashboard

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.pab.scoutify.model.ActiveActivity
import com.pab.scoutify.model.UserLocation
import com.pab.scoutify.ui.dashboard.components.DashboardTopAppBar
import com.pab.scoutify.utils.MapConfig
import androidx.compose.ui.draw.clip

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit,
    onNavigateToSelfie: (Long, Long, Double, Double) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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
            viewModel.loadInitialData()
            viewModel.startLocationUpdates()
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

    // Handle check-in success → navigate to selfie
    LaunchedEffect(uiState.checkInSuccess) {
        if (uiState.checkInSuccess && uiState.lastAttendanceId != null) {
            val activity = uiState.activeActivity
            if (activity != null) {
                onNavigateToSelfie(
                    activity.id,
                    uiState.lastAttendanceId!!,
                    uiState.userLocation?.latitude ?: activity.latitude,
                    uiState.userLocation?.longitude ?: activity.longitude
                )
                viewModel.resetCheckInSuccess()
            }
        }
    }

    // Handle errors
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            DashboardTopAppBar(title = "Presensi GPS", onNotificationClick = onNavigateToNotifications)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F2FA))
        ) {
            if (uiState.isLoading && uiState.activeActivity == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF5E35B1))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))
                    
                    ActiveActivityCard(uiState.activeActivity)

                    MapCard(
                        activeActivity = uiState.activeActivity,
                        userLocation = uiState.userLocation,
                        radius = uiState.activeActivity?.radius ?: 0,
                        isMyLocationEnabled = hasLocationPermission
                    )

                    RadiusStatusCard(
                        isWithinRadius = uiState.isWithinRadius,
                        distance = uiState.distance
                    )

                    // Swipe-to-Confirm Check-In Button
                    AttendanceSwipeButton(
                        status = uiState.attendanceStatus,
                        isEnabled = uiState.isWithinRadius && !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        onCheckIn = {
                            viewModel.checkIn()
                        }
                    )
                    AttendanceFooter(uiState.activeActivity)
                    
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MapCard(
    activeActivity: ActiveActivity?,
    userLocation: UserLocation?,
    radius: Int,
    isMyLocationEnabled: Boolean
) {
    val targetLatLng = remember(activeActivity) {
        if (activeActivity != null && activeActivity.latitude != 0.0) {
            LatLng(activeActivity.latitude, activeActivity.longitude)
        } else {
            MapConfig.defaultLatLng
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLatLng, MapConfig.ATTENDANCE_MAP_ZOOM)
    }

    LaunchedEffect(targetLatLng) {
        if (activeActivity != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(targetLatLng, MapConfig.ATTENDANCE_MAP_ZOOM)
            )
        }
    }

    val mapProperties = remember(isMyLocationEnabled) {
        MapProperties(isMyLocationEnabled = isMyLocationEnabled)
    }
    val mapUiSettings = remember(isMyLocationEnabled) {
        MapUiSettings(
            zoomControlsEnabled = false, 
            myLocationButtonEnabled = isMyLocationEnabled,
            mapToolbarEnabled = true
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            if (activeActivity != null) {
                Marker(
                    state = MarkerState(position = targetLatLng),
                    title = activeActivity.name,
                    snippet = "Titik Presensi"
                )
                Circle(
                    center = targetLatLng,
                    radius = radius.toDouble(),
                    fillColor = Color(0x335E35B1),
                    strokeColor = Color(0xFF5E35B1),
                    strokeWidth = 2f
                )
            }
        }
    }
}

@Composable
fun RadiusStatusCard(isWithinRadius: Boolean, distance: Double) {
    val color = if (isWithinRadius) Color(0xFF2E7D32) else Color(0xFFC62828)
    val bgColor = if (isWithinRadius) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isWithinRadius) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isWithinRadius) "Lokasi Sesuai" else "Di Luar Radius",
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isWithinRadius) "Anda berada di titik kegiatan." else "Jarak: ${distance.toInt()}m. Silakan mendekat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ActiveActivityCard(activity: ActiveActivity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFEFEBFA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Hiking, contentDescription = null, tint = Color(0xFF9C27B0))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = activity?.name ?: "Mencari Kegiatan Aktif...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5E35B1)
                )
                Text(
                    text = activity?.locationName ?: "Lokasi tidak terdeteksi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                if (activity != null) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFEFEBFA),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⏰ ${activity.timeRange}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF5E35B1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceFooter(activity: ActiveActivity?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        AttendanceInfoItem(Icons.Default.AccessTime, activity?.timeRange ?: "--:-- WIB")
        AttendanceInfoItem(Icons.Default.History, "Riwayat")
        AttendanceInfoItem(Icons.AutoMirrored.Filled.HelpOutline, "Bantuan")
    }
}

@Composable
fun AttendanceInfoItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AttendanceSwipeButton(
    status: String,
    isEnabled: Boolean,
    isLoading: Boolean,
    onCheckIn: () -> Unit
) {
    val isAlreadyCheckedIn = status.contains("Sudah", ignoreCase = true)
    
    if (isAlreadyCheckedIn) {
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0),
                disabledContainerColor = Color(0xFF9C27B0)
            ),
            enabled = false
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "✓ Sudah Presensi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    } else {
        SwipeToConfirmButton(
            text = "Geser untuk Presensi",
            onConfirm = onCheckIn,
            isEnabled = isEnabled,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SwipeToConfirmButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isLoading: Boolean = false
) {
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var maxDragDistance by remember { mutableStateOf(0f) }
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val thumbSize = 50.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }
    
    val animatedOffset by animateFloatAsState(
        targetValue = if (isDragging) dragOffset else if (dragOffset >= maxDragDistance * 0.8f) maxDragDistance else 0f,
        animationSpec = androidx.compose.animation.core.spring(),
        label = "dragOffset"
    )

    LaunchedEffect(animatedOffset) {
        if (!isDragging && animatedOffset == maxDragDistance && maxDragDistance > 0f) {
            onConfirm()
            dragOffset = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(if (isEnabled) Color(0xFF5E35B1) else Color.LightGray)
            .onSizeChanged { size ->
                maxDragDistance = size.width - thumbSizePx - with(density) { 12.dp.toPx() }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = if (isLoading) "Memproses..." else text,
            color = if (isEnabled) Color.White.copy(alpha = 0.8f) else Color.DarkGray.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        if (!isLoading && isEnabled) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Color.White)
                    .pointerInput(maxDragDistance) {
                        if (maxDragDistance > 0f) {
                            detectDragGestures(
                                onDragStart = { isDragging = true },
                                onDragEnd = {
                                    isDragging = false
                                    dragOffset = if (dragOffset < maxDragDistance * 0.8f) 0f else maxDragDistance
                                },
                                onDragCancel = {
                                    isDragging = false
                                    dragOffset = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset = (dragOffset + dragAmount.x).coerceIn(0f, maxDragDistance)
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF5E35B1),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
