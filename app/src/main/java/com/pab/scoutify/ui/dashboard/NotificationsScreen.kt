package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pab.scoutify.model.NotificationItem
import com.pab.scoutify.ui.dashboard.components.NotificationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit
) {
    // Data dummy sesuai dengan yang ada di NotificationsFragment sebelumnya
    val notifications = listOf(
        NotificationItem(1, "ABSENSI", "Briefing Keamanan Wajib", "Semua anggota yang mengikuti Perkemahan Hutan Pinus wajib hadir di briefing keamanan Sabtu ini.", "2 jam yang lalu"),
        NotificationItem(2, "INFO", "Syarat Kecakapan Baru Tersedia", "SKU tingkat baru untuk Teknologi Digital sudah tersedia. Hubungi Kak Wilson untuk detailnya.", "5 jam yang lalu"),
        NotificationItem(3, "UPDATE", "Update Pemeriksaan Atribut", "Mohon pastikan semua tanda kecakapan dijahit dengan benar sebelum inspeksi hari Kamis.", "Kemarin"),
        NotificationItem(4, "ABSENSI", "Target Penggalangan Dana Tercapai!", "Selamat! Kita telah melampaui target penggalangan dana untuk renovasi gudep.", "2 hari yang lalu")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Tidak ada notifikasi", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF9F9F6)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}
