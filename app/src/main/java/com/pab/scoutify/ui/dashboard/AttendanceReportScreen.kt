package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import com.pab.scoutify.R
import com.pab.scoutify.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceReportScreen(
    viewModel: AttendanceReportViewModel = hiltViewModel(),
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF9F9F6),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scoutify",
                        color = Color(0xFF1B4332),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF1B4332))
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF1B4332))
                    }
                    IconButton(onClick = onProfileClick) {
                        AsyncImage(
                            model = null, // Profile image URL
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            error = painterResource(id = R.drawable.logo_profil),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ReportHeader(uiState, viewModel) }
            
            uiState.summary?.let { summary ->
                item { MonthlyDistributionCard(summary) }
                item { WeeklyTrendCard(summary.weeklyTrend) }
            }

            item {
                AttendanceAlertBox(
                    message = "Low Attendance Alert: Last Friday's training fell below the 70% threshold. Review list of absent scouts.",
                    onReviewClick = { /* Handle review */ }
                )
            }

            item { RecentActivitySection(uiState.recentActivities) }

            item { TopScoutsSection(uiState.topScouts) }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ReportHeader(state: ReportUiState, viewModel: AttendanceReportViewModel) {
    Column {
        Text(
            text = "Attendance Report",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B4332)
        )
        Text(
            text = "Reviewing scout participation for August 2024",
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    label = state.dateRange,
                    icon = Icons.Default.CalendarToday,
                    onClick = { /* Open date picker */ }
                )
                FilterChip(
                    label = state.selectedTroop,
                    icon = Icons.Default.Groups,
                    onClick = { /* Open troop filter */ }
                )
            }
            
            Button(
                onClick = { viewModel.exportPdf() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FilterChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, color = Color.DarkGray)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        }
    }
}

@Composable
fun MonthlyDistributionCard(summary: AttendanceReportSummary) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Monthly Distribution",
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                DonutChart(summary.distribution)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.overallRate}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4332)
                    )
                    Text(
                        text = "Overall Rate",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                LegendItem("Hadir", "${summary.distribution.hadir}%", Color(0xFF1B4332))
                LegendItem("Izin", "${summary.distribution.izin}%", Color(0xFF8B4513))
                LegendItem("Sakit", "${summary.distribution.sakit}%", Color(0xFFD2B48C))
                LegendItem("Alpa", "${summary.distribution.alpa}%", Color(0xFFC62828))
            }
        }
    }
}

@Composable
fun DonutChart(dist: AttendanceDistribution) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 24.dp.toPx()
        val hadirAngle = (dist.hadir / 100f) * 360f
        val izinAngle = (dist.izin / 100f) * 360f
        val sakitAngle = (dist.sakit / 100f) * 360f
        val alpaAngle = (dist.alpa / 100f) * 360f
        
        var startAngle = -90f
        
        drawArc(Color(0xFF1B4332), startAngle, hadirAngle, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        startAngle += hadirAngle
        drawArc(Color(0xFF8B4513), startAngle, izinAngle, false, style = Stroke(strokeWidth))
        startAngle += izinAngle
        drawArc(Color(0xFFD2B48C), startAngle, sakitAngle, false, style = Stroke(strokeWidth))
        startAngle += sakitAngle
        drawArc(Color(0xFFC62828), startAngle, alpaAngle, false, style = Stroke(strokeWidth))
    }
}

@Composable
fun LegendItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}

@Composable
fun WeeklyTrendCard(trend: List<WeeklyParticipation>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Weekly Participation Trend", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF1B4332)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hadir", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE9ECEF)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Total Member", fontSize = 10.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                trend.forEach { week ->
                    BarChartItem(week)
                }
            }
        }
    }
}

@Composable
fun BarChartItem(week: WeeklyParticipation) {
    val heightFactor = week.attendance.toFloat() / week.total.toFloat()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(Color(0xFFE9ECEF)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFactor)
                    .background(Color(0xFF1B4332))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(week.week, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun AttendanceAlertBox(message: String, onReviewClick: () -> Unit) {
    Surface(
        color = Color(0xFFFDE8E8),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8B4B4))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Low Attendance Alert",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    fontSize = 14.sp
                )
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = Color(0xFFC62828)
                )
                Text(
                    text = "Review List",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    modifier = Modifier.clickable { onReviewClick() }.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<ActivityParticipation>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity Breakdown", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332), fontSize = 18.sp)
            TextButton(onClick = { /* View All */ }) {
                Text("View All", color = Color.Gray, fontSize = 12.sp)
            }
        }
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
        ) {
            Column {
                // Table Header
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA)).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Event Name", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("Date", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("Participation", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                
                activities.forEach { activity ->
                    ActivityRowItem(activity)
                    Divider(color = Color(0xFFF1F3F4))
                }
            }
        }
    }
}

@Composable
fun ActivityRowItem(activity: ActivityParticipation) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1B4332)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Hiking, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(activity.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(activity.location, fontSize = 10.sp, color = Color.Gray)
            }
        }
        
        Text(activity.date, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.DarkGray)
        
        Column(modifier = Modifier.weight(1.5f)) {
            LinearProgressIndicator(
                progress = activity.rate / 100f,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (activity.rate >= 80) Color(0xFF1B4332) else Color(0xFF8B4513),
                trackColor = Color(0xFFE9ECEF)
            )
            Text("${activity.rate}%", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
fun TopScoutsSection(scouts: List<ScoutParticipation>) {
    Column {
        Text(
            "Top Participation Scouts",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B4332),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFE9ECEF).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                scouts.forEach { scout ->
                    ScoutLeaderboardItem(scout)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                OutlinedButton(
                    onClick = { /* Full Leaderboard */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Text("Full Leaderboard", color = Color.DarkGray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ScoutLeaderboardItem(scout: ScoutParticipation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(scout.color))),
                contentAlignment = Alignment.Center
            ) {
                Text(scout.initial, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(scout.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Text("${scout.rate}%", fontWeight = FontWeight.Bold, color = Color(0xFF1B4332), fontSize = 14.sp)
    }
}
