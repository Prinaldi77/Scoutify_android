package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    viewModel: AddActivityViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    // State untuk Peta
    val jakarta = LatLng(-6.200000, 106.816666)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(jakarta, 15f)
    }

    // Update ViewModel saat marker di peta digeser atau peta diklik
    LaunchedEffect(cameraPositionState.position.target) {
        viewModel.onLocationChange(
            cameraPositionState.position.target.latitude,
            cameraPositionState.position.target.longitude
        )
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9F9F6),
        topBar = {
            TopAppBar(
                title = { Text("Tambah Kegiatan Baru", fontWeight = FontWeight.Bold) },
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
            Text("Informasi Kegiatan", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332))
            
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nama Kegiatan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.date,
                    onValueChange = { viewModel.onDateChange(it) },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
                )
                OutlinedTextField(
                    value = uiState.time,
                    onValueChange = { viewModel.onTimeChange(it) },
                    label = { Text("Waktu (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.AccessTime, null) }
                )
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
            Divider()
            Text("Lokasi & Geofence", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332))
            
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
                        fillColor = Color(0x221B4332),
                        strokeColor = Color(0xFF1B4332),
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
                        thumbColor = Color(0xFF1B4332),
                        activeTrackColor = Color(0xFF1B4332)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
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
