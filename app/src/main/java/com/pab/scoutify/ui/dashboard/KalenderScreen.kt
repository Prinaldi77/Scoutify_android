package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pab.scoutify.model.ActivityItem
import java.util.Calendar

val INDONESIAN_MONTHS = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
)

val DAYS_OF_WEEK = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalenderScreen(
    viewModel: ActivitiesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Calendar View States
    val currentCal = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableIntStateOf(currentCal.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCal.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf<Int?>(currentCal.get(Calendar.DAY_OF_MONTH)) }

    LaunchedEffect(Unit) {
        viewModel.loadActivities()
    }

    // Date matching helper
    fun isDateMatching(dateString: String, year: Int, month: Int, day: Int): Boolean {
        return try {
            val parts = dateString.split("-")
            if (parts.size >= 3) {
                val y = parts[0].toInt()
                val m = parts[1].toInt() - 1 // 1-indexed month to 0-indexed month
                val d = parts[2].substring(0, 2).toInt() // strip any time suffix if present
                y == year && m == month && d == day
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Helper to calculate first day of the week and total days in the month
    val calendarHelper = remember(selectedMonth, selectedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val rawStartDay = cal.get(Calendar.DAY_OF_WEEK)
        // Convert Sunday=1, Monday=2... to 0-indexed starting Monday
        val offset = if (rawStartDay == Calendar.SUNDAY) 6 else rawStartDay - 2
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Pair(offset, daysInMonth)
    }
    
    val startDayOffset = calendarHelper.first
    val daysInMonth = calendarHelper.second

    // Filter activities matching the selected month & year
    val monthActivities = uiState.activities.filter { activity ->
        try {
            val parts = activity.date.split("-")
            if (parts.size >= 2) {
                val y = parts[0].toInt()
                val m = parts[1].toInt() - 1
                y == selectedYear && m == selectedMonth
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Filter activities matching the specifically selected day
    val selectedDayActivities = if (selectedDay == null) emptyList() else {
        monthActivities.filter { isDateMatching(it.date, selectedYear, selectedMonth, selectedDay!!) }
    }

    Scaffold(
        containerColor = Color(0xFFF5F2FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kalender Kegiatan",
                        color = Color(0xFF5E35B1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF5E35B1))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Month Picker Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (selectedMonth == 0) {
                                selectedMonth = 11
                                selectedYear -= 1
                            } else {
                                selectedMonth -= 1
                            }
                            selectedDay = null // reset day selection on month change
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya", tint = Color(0xFF5E35B1))
                        }

                        Text(
                            text = "${INDONESIAN_MONTHS[selectedMonth]} $selectedYear",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        IconButton(onClick = {
                            if (selectedMonth == 11) {
                                selectedMonth = 0
                                selectedYear += 1
                            } else {
                                selectedMonth += 1
                            }
                            selectedDay = null
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Selanjutnya", tint = Color(0xFF5E35B1))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weekly days header (Sen, Sel, Rab, etc.)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DAYS_OF_WEEK.forEach { dayName ->
                            Text(
                                text = dayName,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days grid
                    val totalCells = 42 // 6 rows of 7 days
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0 until 6) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - startDayOffset + 1
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dayNum in 1..daysInMonth) {
                                            val isSelected = selectedDay == dayNum
                                            val hasActivities = monthActivities.any { isDateMatching(it.date, selectedYear, selectedMonth, dayNum) }
                                            
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) Color(0xFF5E35B1) else Color.Transparent
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (hasActivities && !isSelected) Color(0xFF5E35B1).copy(alpha = 0.5f) else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        selectedDay = dayNum
                                                    }
                                            ) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected || hasActivities) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else if (hasActivities) Color(0xFF5E35B1) else Color.Black
                                                )
                                                if (hasActivities) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else Color(0xFF5E35B1))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Activities detail listing
            Text(
                text = if (selectedDay == null) "Agenda Kegiatan Bulan Ini" else "Agenda Kegiatan Tanggal $selectedDay ${INDONESIAN_MONTHS[selectedMonth]}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF5E35B1))
                }
            } else {
                val listToRender = if (selectedDay == null) monthActivities else selectedDayActivities
                
                if (listToRender.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedDay == null) "Tidak ada latihan dijadwalkan bulan ini." else "Tidak ada latihan dijadwalkan hari ini.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listToRender) { activity ->
                            ActivityTimelineCard(activity)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityTimelineCard(activity: ActivityItem) {
    val isPast = activity.category.equals("selesai", ignoreCase = true) || activity.category.equals("nonaktif", ignoreCase = true)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isPast) Color.Gray else Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activity.date,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = activity.location,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Status Badge
            Surface(
                color = if (isPast) Color(0xFFE9ECEF) else Color(0xFFEFEBFA),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isPast) "SELESAI" else "AKTIF",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPast) Color.Gray else Color(0xFF5E35B1)
                )
            }
        }
    }
}
