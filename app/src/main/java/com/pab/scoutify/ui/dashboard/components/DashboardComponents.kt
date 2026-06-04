package com.pab.scoutify.ui.dashboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pab.scoutify.R
import com.pab.scoutify.model.NotificationItem
import com.pab.scoutify.model.ProfileData
import com.pab.scoutify.model.UpcomingActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopAppBar(
    title: String = "Scoutify",
    onNotificationClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5E35B1),
                    letterSpacing = 1.sp
                )
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = { Badge(containerColor = Color.Red) { Text("2", color = Color.White) } }
                ) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = Color(0xFF5E35B1))
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        ),
        modifier = Modifier.shadow(2.dp)
    )
}

@Composable
fun HeaderUserSection(userProfile: ProfileData?) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF5E35B1), Color(0xFF9C27B0))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.background(gradientBrush).padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Salam Pramuka!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = userProfile?.name ?: "Kakak Pandu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = userProfile?.role?.uppercase() ?: "ANGGOTA",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                AsyncImage(
                    model = userProfile?.avatar,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                        .padding(2.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.logo_profil)
                )
            }
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Unspecified,
    height: androidx.compose.ui.unit.Dp = 115.dp
) {
    val finalContentColor = if (contentColor != Color.Unspecified) contentColor else Color(0xFF5E35B1)
    
    Card(
        modifier = modifier
            .height(height)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, modifier = Modifier.size(20.dp), tint = finalContentColor)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(12.dp), tint = finalContentColor.copy(alpha = 0.3f))
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = finalContentColor)
                Text(title, style = MaterialTheme.typography.labelSmall, color = finalContentColor.copy(alpha = 0.7f), maxLines = 1)
            }
        }
    }
}

@Composable
fun UpcomingActivityCard(
    activity: UpcomingActivity,
    onCheckInClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clickable { onCheckInClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.height(150.dp).fillMaxWidth()) {
                AsyncImage(
                    model = activity.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.bg_gradient_brown)
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
                
                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD8B1)
                ) {
                    Text(activity.status.uppercase(), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF8B4513))
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(activity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF5E35B1))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), tint = Color(0xFF9C27B0))
                    Spacer(Modifier.width(6.dp))
                    Text(activity.location, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCheckInClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1))
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Buka Presensi", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (notification.category == "ABSENSI") Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (notification.category == "ABSENSI") Icons.AutoMirrored.Filled.FactCheck else Icons.Default.Campaign,
                        contentDescription = null,
                        tint = if (notification.category == "ABSENSI") Color(0xFF2E7D32) else Color(0xFFF57F17),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(notification.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(notification.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Text(notification.content, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5E35B1))
        TextButton(onClick = onSeeAllClick) {
            Text("Lihat Semua", fontWeight = FontWeight.Bold, color = Color(0xFF9C27B0))
        }
    }
}
