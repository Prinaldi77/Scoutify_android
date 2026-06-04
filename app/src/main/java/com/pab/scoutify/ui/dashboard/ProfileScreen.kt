package com.pab.scoutify.ui.dashboard

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pab.scoutify.R
import com.pab.scoutify.model.ChangePasswordRequest
import com.pab.scoutify.model.ProfileData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    // Form states for Edit Profile
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }

    // Form states for Change Password
    var oldPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadPhoto(it) }
    }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            onLogoutSuccess()
        }
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            showEditDialog = false
            showPasswordDialog = false
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, "Gagal: $it", Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    // Prefill form states when profile loads
    LaunchedEffect(uiState.profile) {
        uiState.profile?.let {
            nameInput = it.name ?: ""
            phoneInput = it.phone ?: ""
            emailInput = it.email ?: ""
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F2FA),
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", fontWeight = FontWeight.Bold, color = Color(0xFF5E35B1)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF5E35B1))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF5E35B1))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ProfileHeaderCard(
                    profile = uiState.profile,
                    onAddPhotoClick = { imagePickerLauncher.launch("image/*") }
                )

                InfoMembershipSection(profile = uiState.profile)

                AccountSettingsSection(
                    onEditProfile = { showEditDialog = true },
                    onChangePassword = { showPasswordDialog = true },
                    onLogout = { showLogoutDialog = true }
                )

                Text(
                    text = "Scoutify v2.4.0\nDibuat dengan semangat kepanduan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color(0xFF5E35B1))
            }
        }
    }

    // Modal Edit Profil
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Nomor Telepon") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(nameInput, phoneInput, emailInput, null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1))
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Ganti Password
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Ganti Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = oldPasswordInput,
                        onValueChange = { oldPasswordInput = it },
                        label = { Text("Password Lama") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("Password Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Konfirmasi Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPasswordInput != confirmPasswordInput) {
                            Toast.makeText(context, "Password baru tidak cocok!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.changePassword(ChangePasswordRequest(oldPasswordInput, newPasswordInput, confirmPasswordInput))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1))
                ) {
                    Text("Perbarui")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(onClick = { 
                    showLogoutDialog = false
                    viewModel.logout() 
                }) {
                    Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ProfileHeaderCard(profile: ProfileData?, onAddPhotoClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AsyncImage(
                    model = profile?.avatar,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.logo_profil)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9C27B0))
                        .clickable { onAddPhotoClick() }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = profile?.name ?: "User Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5E35B1)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF5E35B1),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = profile?.role ?: "Member",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "NISN: ${profile?.nomorInduk ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun InfoMembershipSection(profile: ProfileData?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("INFORMASI KEANGGOTAAN", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
        ) {
            Column {
                MembershipItem(Icons.Default.Groups, "Gugus Depan", profile?.gugusDepan ?: "-")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3F5))
                MembershipItem(Icons.Default.Groups, "Ambalan/Regu", profile?.regu ?: "-")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3F5))
                MembershipItem(Icons.Default.Phone, "Phone Number", profile?.phone ?: "-")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3F5))
                MembershipItem(Icons.Default.Email, "Email", profile?.email ?: "-")
            }
        }
    }
}

@Composable
fun MembershipItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF5F2FA), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF5E35B1), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AccountSettingsSection(onEditProfile: () -> Unit, onChangePassword: () -> Unit, onLogout: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("PENGATURAN AKUN", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
        ) {
            Column {
                ProfileMenuItem(Icons.Default.Edit, "Edit Profil", onClick = onEditProfile)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3F5))
                ProfileMenuItem(Icons.Default.History, "Ganti Password", onClick = onChangePassword)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3F5))
                ProfileMenuItem(Icons.AutoMirrored.Filled.Logout, "Logout", textColor = Color.Red, onClick = onLogout)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, textColor: Color = Color.Unspecified, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (textColor != Color.Unspecified) textColor else Color(0xFF5E35B1))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}
