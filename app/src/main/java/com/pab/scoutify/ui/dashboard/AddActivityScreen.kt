package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.pab.scoutify.utils.MapConfig
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    viewModel: AddActivityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Date Picker Dialog Setup
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            viewModel.onDateChange(formattedDate)
        },
        try {
            val parts = uiState.date.split("-")
            parts[0].toInt()
        } catch (e: Exception) {
            calendar.get(Calendar.YEAR)
        },
        try {
            val parts = uiState.date.split("-")
            parts[1].toInt() - 1
        } catch (e: Exception) {
            calendar.get(Calendar.MONTH)
        },
        try {
            val parts = uiState.date.split("-")
            parts[2].toInt()
        } catch (e: Exception) {
            calendar.get(Calendar.DAY_OF_MONTH)
        }
    )

    // Time Picker Dialog Setup
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            viewModel.onTimeChange(formattedTime)
        },
        try {
            val parts = uiState.time.split(":")
            parts[0].toInt()
        } catch (e: Exception) {
            calendar.get(Calendar.HOUR_OF_DAY)
        },
        try {
            val parts = uiState.time.split(":")
            parts[1].toInt()
        } catch (e: Exception) {
            calendar.get(Calendar.MINUTE)
        },
        true
    )
    
    // State untuk Peta
    val defaultLoc = MapConfig.defaultLatLng
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLoc, MapConfig.DEFAULT_ZOOM)
    }

    // Sync map camera when loaded from ViewModel (only if target is different to prevent jump loop)
    LaunchedEffect(uiState.latitude, uiState.longitude) {
        val currentTarget = cameraPositionState.position.target
        if (Math.abs(currentTarget.latitude - uiState.latitude) > 0.0001 || 
            Math.abs(currentTarget.longitude - uiState.longitude) > 0.0001) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(uiState.latitude, uiState.longitude),
                MapConfig.DEFAULT_ZOOM
            )
        }
    }

    // Throttled: Update ViewModel saat marker di peta selesai digeser
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            viewModel.onLocationChange(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude
            )
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F2FA),
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Edit Kegiatan" else "Tambah Kegiatan Baru", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Informasi Dasar
            Text("Informasi Kegiatan", fontWeight = FontWeight.Bold, color = Color(0xFF5E35B1))
            
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nama Kegiatan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { datePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = uiState.date,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = uiState.time,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Waktu") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Deskripsi Singkat") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // Section 2: Lokasi & Geofence
            HorizontalDivider()
            Text("Lokasi & Geofence", fontWeight = FontWeight.Bold, color = Color(0xFF5E35B1))
            
            OutlinedTextField(
                value = uiState.locationName,
                onValueChange = { viewModel.onLocationNameChange(it) },
                label = { Text("Nama Tempat / Lokasi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Place, null) }
            )

            // Map Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = true)
                ) {
                    Marker(
                        state = MarkerState(position = cameraPositionState.position.target),
                        title = "Lokasi Presensi",
                        draggable = true
                    )
                    Circle(
                        center = cameraPositionState.position.target,
                        radius = uiState.radius.toDouble(),
                        fillColor = Color(0x225E35B1),
                        strokeColor = Color(0xFF5E35B1),
                        strokeWidth = 2f
                    )
                }
                // Center Crosshair
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    tint = Color.Red
                )
            }
            
            Text(
                "Koordinat: ${uiState.latitude}, ${uiState.longitude}",
                fontSize = 11.sp,
                color = Color.Gray
            )

            // Slider Radius
            Column {
                Text("Radius Presensi: ${uiState.radius.toInt()} meter", fontSize = 14.sp)
                Slider(
                    value = uiState.radius,
                    onValueChange = { viewModel.onRadiusChange(it) },
                    valueRange = 50f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF5E35B1),
                        activeTrackColor = Color(0xFF5E35B1)
                    )
                )
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = Color.Red, fontSize = 12.sp)
            }

            Button(
                onClick = { viewModel.submitActivity() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SIMPAN KEGIATAN", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
