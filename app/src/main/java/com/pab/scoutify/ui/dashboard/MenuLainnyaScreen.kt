package com.pab.scoutify.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.model.Piket
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuLainnyaScreen(
    onNavigateToProfile: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    var activeMenu by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = Color(0xFFF5F2FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (activeMenu == null) "Menu Lainnya" else when (activeMenu) {
                            0 -> "Jadwal Piket"
                            1 -> "Peraturan Organisasi"
                            2 -> "Leaderboard Poin"
                            else -> "Tentang Aplikasi"
                        },
                        color = Color(0xFF5E35B1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    if (activeMenu != null) {
                        IconButton(onClick = { activeMenu = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF5E35B1))
                        }
                    }
                },
                actions = {
                    if (activeMenu == null) {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profil Saya", tint = Color(0xFF5E35B1))
                        }
                        IconButton(onClick = onLogoutSuccess) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Keluar", tint = Color(0xFFC62828))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Main Dashboard Grid with animations
            AnimatedVisibility(
                visible = activeMenu == null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF5E35B1))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                "Fitur Pendukung",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Akses modul tugas piket, peraturan resmi, peringkat keaktifan, dan tentang aplikasi Scoutify.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2x2 Grid of Menu Items
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1.0f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MenuGridItem(
                            title = "Jadwal Piket",
                            description = "Jadwal regu piket harian",
                            icon = Icons.Default.CleaningServices,
                            color = Color(0xFF1E88E5),
                            modifier = Modifier.weight(1.0f),
                            onClick = { activeMenu = 0 }
                        )
                        MenuGridItem(
                            title = "Peraturan",
                            description = "Tri Satya & Dasa Darma",
                            icon = Icons.Default.Assignment,
                            color = Color(0xFF8E24AA),
                            modifier = Modifier.weight(1.0f),
                            onClick = { activeMenu = 1 }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1.0f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MenuGridItem(
                            title = "Leaderboard",
                            description = "Peringkat keaktifan siswa",
                            icon = Icons.Default.Star,
                            color = Color(0xFFFFB300),
                            modifier = Modifier.weight(1.0f),
                            onClick = { activeMenu = 2 }
                        )
                        MenuGridItem(
                            title = "Tentang",
                            description = "Detail lisensi & info aplikasi",
                            icon = Icons.Default.Info,
                            color = Color(0xFF00897B),
                            modifier = Modifier.weight(1.0f),
                            onClick = { activeMenu = 3 }
                        )
                    }
                }
            }

            // Detailed Content Overlay with slide-in animation
            AnimatedVisibility(
                visible = activeMenu != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (activeMenu) {
                        0 -> JadwalPiketTab()
                        1 -> PeraturanTab()
                        2 -> LeaderboardTab()
                        3 -> TentangTab()
                    }
                }
            }
        }
    }
}

@Composable
fun MenuGridItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
        }
    }
}

@Composable
fun JadwalPiketTab() {
    var piketList by remember { mutableStateOf<List<Piket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.getPiket()
                if (response.isSuccessful && response.body() != null) {
                    piketList = response.body()!!.data
                } else {
                    errorMessage = "Gagal memuat jadwal piket."
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Kesalahan koneksi jaringan."
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF5E35B1))
        }
    } else if (errorMessage != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage!!, color = Color.Gray, textAlign = TextAlign.Center)
        }
    } else if (piketList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Jadwal piket belum diatur oleh pembina.", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(piketList) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFEFEBFA),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color(0xFF5E35B1))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(item.hari, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(" Putra: ${item.reguPutra}", fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        labelColor = Color(0xFF1E88E5)
                                    )
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(" Putri: ${item.reguPutri}", fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        labelColor = Color(0xFFD81B60)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeraturanTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TRI SATYA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF5E35B1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Demi kehormatanku aku berjanji akan bersungguh-sungguh:\n" +
                                "1. Menjalankan kewajibanku terhadap Tuhan Yang Maha Esa, Negara Kesatuan Republik Indonesia dan mengamalkan Pancasila.\n" +
                                "2. Menolong sesama hidup dan mempersiapkan diri membangun masyarakat.\n" +
                                "3. Menepati Dasa Darma.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DASA DARMA PRAMUKA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF5E35B1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val darmas = listOf(
                        "1. Takwa kepada Tuhan Yang Maha Esa.",
                        "2. Cinta alam dan kasih sayang sesama manusia.",
                        "3. Patriot yang sopan dan kesatria.",
                        "4. Patuh dan suka bermusyawarah.",
                        "5. Rela menolong dan tabah.",
                        "6. Rajin, terampil, dan gembira.",
                        "7. Hemat, cermat, dan bersahaja.",
                        "8. Disiplin, berani, dan setia.",
                        "9. Bertanggung jawab dan dapat dipercaya.",
                        "10. Suci dalam pikiran, perkataan, dan perbuatan."
                    )
                    darmas.forEach { darma ->
                        Text(
                            text = darma,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardTab() {
    val rankList = listOf(
        Triple("1", "Muhamad Prinaldi", "95 Poin"),
        Triple("2", "Siti Rahma", "90 Poin"),
        Triple("3", "Budi Santoso", "85 Poin"),
        Triple("4", "Aldi Saputra", "82 Poin"),
        Triple("5", "Amanda Lestari", "80 Poin")
    )
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(rankList) { index, item ->
            val isTop3 = index < 3
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index == 0) Color(0xFFFFF9C4) else Color.White // gold highlight for rank 1
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Medal / Rank number
                    Surface(
                        color = if (isTop3) Color(0xFF5E35B1) else Color(0xFFE9ECEF),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.first,
                                fontWeight = FontWeight.Bold,
                                color = if (isTop3) Color.White else Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.second,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.third,
                        color = Color(0xFF5E35B1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TentangTab() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFFEFEBFA),
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF5E35B1)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Scoutify App", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF5E35B1))
            Text("Versi 2.0-katapang", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFE9ECEF))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aplikasi Sistem Informasi Absensi GPS Geofencing & Verifikasi Wajah Biometrik Gerakan Pramuka Gugus Depan SMP Negeri 2 Katapang.",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "© 2026 Tim Pengembang Pramuka SMPN 2 Katapang",
                fontSize = 12.sp,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }
    }
}
