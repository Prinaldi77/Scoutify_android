package com.pab.scoutify.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.pab.scoutify.ui.dashboard.components.SelfieOverlay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfieVerificationScreen(
    activityId: Long,
    attendanceId: Long,
    latitude: Double,
    longitude: Double,
    viewModel: SelfieVerificationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val imageAnalyzer = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                try {
                    cameraProvider = future.get()
                } catch (e: Exception) {
                    Log.e("SelfieScreen", "Failed to get camera provider", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    LaunchedEffect(lensFacing, cameraProvider, hasCameraPermission) {
        val provider = cameraProvider
        if (hasCameraPermission && provider != null) {
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                viewModel.onImageAnalyzed(
                    imageProxy,
                    imageProxy.width,
                    imageProxy.height
                )
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("SelfieScreen", "Use case binding failed", e)
            }
        }
    }

    LaunchedEffect(uiState.verificationSuccess) {
        if (uiState.verificationSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verifikasi Selfie", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show help */ }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (hasCameraPermission) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { previewView ->
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        }
                    )

                    SelfieOverlay(isFaceDetected = uiState.isFaceDetected)

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.faceDetectionMessage,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 32.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                    CameraSelector.LENS_FACING_BACK
                                } else {
                                    CameraSelector.LENS_FACING_FRONT
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.FlipCameraAndroid, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Gunakan Kamera ${if (lensFacing == CameraSelector.LENS_FACING_FRONT) "Belakang" else "Depan"}")
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isReadyForVerification) Color.White else Color.Gray.copy(alpha = 0.5f))
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5E35B1))
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.captureAndUpload(
                                        imageCapture,
                                        activityId,
                                        attendanceId,
                                        latitude,
                                        longitude
                                    )
                                },
                                modifier = Modifier.fillMaxSize(),
                                enabled = uiState.isReadyForVerification && !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(color = Color.White)
                                } else {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Capture",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isReadyForVerification) Color.Green else Color.Gray)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isReadyForVerification) "Siap untuk verifikasi" else "Belum siap",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Izin kamera diperlukan untuk fitur ini",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            // Error Dialog
            if (uiState.errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.resetError() },
                    title = { Text("Verifikasi Gagal") },
                    text = { Text(uiState.errorMessage!!) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.resetError() }) {
                            Text("Tutup")
                        }
                    }
                )
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}
