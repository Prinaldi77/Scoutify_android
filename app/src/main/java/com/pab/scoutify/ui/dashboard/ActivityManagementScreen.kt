package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pab.scoutify.R
import com.pab.scoutify.model.Kegiatan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityManagementScreen(
    viewModel: ActivityManagementViewModel = hiltViewModel(),
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAddActivityClick: () -> Unit = {},
    onEditActivity: (Long) -> Unit = {},
    onCheckDetail: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF5F2FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scoutify",
                        color = Color(0xFF5E35B1),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF5E35B1))
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF5E35B1))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddActivityClick,
                containerColor = Color(0xFF5E35B1),
                contentColor = Color.White,
                shape = RoundedCornerShape(30.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("TAMBAH KEGIATAN", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header Section
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Manajemen Kegiatan",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5E35B1)
                )
                Text(
                    text = "Kelola daftar aktivitas kepramukaan Pembina.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Tab Filter
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    label = "Aktif",
                    icon = Icons.Default.CalendarToday,
                    isSelected = uiState.selectedTab == "Aktif",
                    onClick = { viewModel.onTabSelected("Aktif") }
                )
                StatusChip(
                    label = "Selesai",
                    icon = Icons.Default.History,
                    isSelected = uiState.selectedTab == "Selesai",
                    onClick = { viewModel.onTabSelected("Selesai") }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF5E35B1))
                }
            } else if (uiState.activities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada kegiatan", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.activities) { kegiatan ->
                        if (uiState.selectedTab == "Aktif") {
                            ActiveManagementCard(
                                kegiatan = kegiatan,
                                onEdit = { onEditActivity(kegiatan.id ?: 0) },
                                onDelete = { viewModel.deleteActivity(kegiatan.id ?: 0) },
                                onDetail = { onCheckDetail(kegiatan.id ?: 0) }
                            )
                        } else {
                            FinishedManagementCard(
                                kegiatan = kegiatan,
                                onEdit = { onEditActivity(kegiatan.id ?: 0) },
                                onDelete = { viewModel.deleteActivity(kegiatan.id ?: 0) },
                                onDetail = { onCheckDetail(kegiatan.id ?: 0) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF5E35B1) else Color(0xFFE9ECEF)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color.White else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ActiveManagementCard(
    kegiatan: Kegiatan,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDetail: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column {
            Box {
                // Image Banner - Using a placeholder or actual image URL if added to Kegiatan model
                AsyncImage(
                    model = "https://placeholder.com/banner", // Replace with real URL
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_background) // Placeholder
                )
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF5E35B1).copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "Pagi",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = kegiatan.nama ?: "Nama Kegiatan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                        color = Color.Black
                    )
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = kegiatan.lokasi ?: "Lokasi", color = Color.Gray, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Radius Presensi: ${kegiatan.radius?.toInt() ?: 0}m", color = Color.Gray, fontSize = 13.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF1F3F4))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "24 Peserta", color = Color.Gray, fontSize = 12.sp)
                    }
                    Text(
                        text = "Cek Detail",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onDetail() }
                    )
                }
            }
        }
    }
}

@Composable
fun FinishedManagementCard(
    kegiatan: Kegiatan,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDetail: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE9ECEF)
                ) {
                    Text(
                        text = "Selesai",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = kegiatan.nama ?: "Nama Kegiatan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = kegiatan.tanggal ?: "Tanggal", color = Color.Gray, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = kegiatan.lokasi ?: "Lokasi", color = Color.Gray, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD8B1).copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF8B4513))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Selesai", color = Color(0xFF8B4513), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Laporan",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onDetail() }
                )
            }
        }
    }
}
