package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
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
import com.pab.scoutify.model.Achievement
import com.pab.scoutify.model.ActivityHistoryItem
import com.pab.scoutify.model.MemberDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    viewModel: MemberDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEditData: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF9F9F6),
        topBar = {
            TopAppBar(
                title = { Text("Member Detail", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1B4332))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF1B4332))
                    }
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color(0xFF1B4332))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = false,
                    onClick = { /* Navigate to Dashboard */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Attendance") },
                    label = { Text("Attendance") },
                    selected = false,
                    onClick = { /* Navigate to Attendance */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Management") },
                    label = { Text("Management") },
                    selected = true,
                    onClick = { /* Already here or navigate back */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFF1B4332),
                        indicatorColor = Color(0xFF1B4332)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                    label = { Text("Reports") },
                    selected = false,
                    onClick = { /* Navigate to Reports */ }
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1B4332))
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.errorMessage!!, color = Color.Red)
            }
        } else {
            uiState.member?.let { member ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderSection(
                        member = member,
                        onEditData = { onEditData(member.id) },
                        onResetPassword = { viewModel.resetPassword() }
                    )

                    PersonalInformationSection(member)

                    ScoutAchievementsSection(member.achievements)

                    ActivityHistorySection(
                        history = member.activityHistory,
                        attendanceRate = member.attendanceRate
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    member: MemberDetail,
    onEditData: () -> Unit,
    onResetPassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F0).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AsyncImage(
                    model = member.fotoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.logo_profil)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .offset(x = (-4).dp, y = (-4).dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2D6A4F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = member.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B4332)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color(0xFF1B4332),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = member.rank,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "NISN: ${member.nisn}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEditData,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Data", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onResetPassword,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reset Password", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PersonalInformationSection(member: MemberDetail) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF1B4332), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Personal Information", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoItem("PLACE / DATE OF BIRTH", member.birthInfo ?: "-")
            InfoItem("PHONE NUMBER", member.phone ?: "-")
            InfoItem("HOME ADDRESS", member.address ?: "-")
            InfoItem("BLOOD TYPE", member.bloodType ?: "-")
            InfoItem("REGIMENT (REGU)", member.regu ?: "-")
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
fun ScoutAchievementsSection(achievements: List<Achievement>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF1B4332), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scout Achievements", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            achievements.forEach { achievement ->
                AchievementBadge(achievement)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AchievementBadge(achievement: Achievement) {
    Surface(
        color = Color(0xFFF8F9FA),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE9ECEF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (achievement.title.contains("First Aid")) Icons.Default.MedicalServices else Icons.Default.Explore,
                    contentDescription = null,
                    tint = Color(0xFF8B4513),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(achievement.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Earned ${achievement.date}", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ActivityHistorySection(history: List<ActivityHistoryItem>, attendanceRate: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = Color(0xFF1B4332), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activity History", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332), fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$attendanceRate%", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332), fontSize = 14.sp)
                    Text("ATTENDANCE RATE", fontSize = 8.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            history.take(4).forEach { item ->
                HistoryListItem(item)
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* View Full History */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F3F4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("View Full History", color = Color.DarkGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryListItem(item: ActivityHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B4332)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when (item.type) {
                    "Survival" -> Icons.Default.Park
                    "Ceremony" -> Icons.Default.Celebration
                    "Service" -> Icons.Default.CleaningServices
                    else -> Icons.Default.Hiking
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(item.date, fontSize = 12.sp, color = Color.Gray)
        }
        val (bg, fg) = when (item.status) {
            "Present" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
            "Late" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
            else -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        }
        Surface(
            color = bg,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(fg))
                Spacer(modifier = Modifier.width(4.dp))
                Text(item.status, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
