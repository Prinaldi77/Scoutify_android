package com.pab.scoutify.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    viewModel: AddMemberViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var jabatanDropdownExpanded by remember { mutableStateOf(false) }

    val rolesList = listOf("SISWA", "BENDAHARA", "SEKRETARIS", "PEMBINA")
    val jabatansList = listOf("Pratama", "Wakil Pratama", "Bendahara", "Sekretaris", "Pembina Putra", "Pembina Putri", "Anggota")

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F2FA),
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Edit Anggota" else "Tambah Anggota Baru", fontWeight = FontWeight.Bold) },
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
            Text("Informasi Akun & Personal", fontWeight = FontWeight.Bold, color = Color(0xFF5E35B1))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email (digunakan untuk login)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isEditMode,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )

            OutlinedTextField(
                value = uiState.nisn,
                onValueChange = { viewModel.onNisnChange(it) },
                label = { Text("NISN / Nomor Induk") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Badge, null) }
            )

            HorizontalDivider()
            Text("Hak Akses & Kepramukaan", fontWeight = FontWeight.Bold, color = Color(0xFF5E35B1))

            // Dropdown untuk Role Akses System
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = when (uiState.role) {
                        "SISWA" -> "Siswa (Akses Standar)"
                        "BENDAHARA" -> "Bendahara (Akses Uang Kas)"
                        "SEKRETARIS" -> "Sekretaris (Akses Laporan)"
                        "PEMBINA" -> "Pembina (Akses Manajemen)"
                        else -> uiState.role
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Akses / Role System") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { roleDropdownExpanded = true },
                    enabled = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Security, null) },
                    trailingIcon = {
                        IconButton(onClick = { roleDropdownExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    rolesList.forEach { roleVal ->
                        DropdownMenuItem(
                            text = { 
                                Text(when (roleVal) {
                                    "SISWA" -> "Siswa (Akses Standar)"
                                    "BENDAHARA" -> "Bendahara (Akses Uang Kas)"
                                    "SEKRETARIS" -> "Sekretaris (Akses Laporan)"
                                    "PEMBINA" -> "Pembina (Akses Manajemen)"
                                    else -> roleVal
                                })
                            },
                            onClick = {
                                viewModel.onRoleChange(roleVal)
                                roleDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Dropdown untuk Jabatan Kepramukaan
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.jabatan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jabatan Kepramukaan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { jabatanDropdownExpanded = true },
                    enabled = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Stars, null) },
                    trailingIcon = {
                        IconButton(onClick = { jabatanDropdownExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = jabatanDropdownExpanded,
                    onDismissRequest = { jabatanDropdownExpanded = false }
                ) {
                    jabatansList.forEach { jabatanVal ->
                        DropdownMenuItem(
                            text = { Text(jabatanVal) },
                            onClick = {
                                viewModel.onJabatanChange(jabatanVal)
                                // Sync default role for ease of use
                                if (jabatanVal == "Bendahara") viewModel.onRoleChange("BENDAHARA")
                                else if (jabatanVal == "Sekretaris") viewModel.onRoleChange("SEKRETARIS")
                                else if (jabatanVal.startsWith("Pembina")) viewModel.onRoleChange("PEMBINA")
                                else viewModel.onRoleChange("SISWA")
                                
                                jabatanDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.rank,
                onValueChange = { viewModel.onRankChange(it) },
                label = { Text("Tingkatan / Pangkat (Contoh: Penggalang, Penegak)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Leaderboard, null) }
            )

            OutlinedTextField(
                value = uiState.regu,
                onValueChange = { viewModel.onReguChange(it) },
                label = { Text("Regu / Ambalan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Groups, null) }
            )

            // Status Selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status: ", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = uiState.status == "Aktif",
                    onClick = { viewModel.onStatusChange("Aktif") },
                    label = { Text("Aktif") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = uiState.status == "Non-aktif",
                    onClick = { viewModel.onStatusChange("Non-aktif") },
                    label = { Text("Non-aktif") }
                )
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = Color.Red, fontSize = 12.sp)
            }

            Button(
                onClick = { viewModel.submitMember() },
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
                    Text("SIMPAN ANGGOTA", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
