package com.app.qrcode

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import com.app.qrcode.ui.theme.Pink
import com.app.qrcode.ui.theme.QrCodeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrCodeTheme {
                var code by remember {
                    mutableStateOf("")
                }
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val cameraProviderFuture = remember {
                    ProcessCameraProvider.getInstance(context)
                }
                var hasCamPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        hasCamPermission = granted
                    }
                )
                LaunchedEffect(key1 = true) {
                    launcher.launch(Manifest.permission.CAMERA)
                }
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                    val (cam, box) = createRefs()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .constrainAs(cam) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                    ) {
                        if (hasCamPermission) {
                            AndroidView(
                                factory = { context ->
                                    val previewView = PreviewView(context)
                                    val preview = Preview.Builder().build()
                                    val selector = CameraSelector.Builder()
                                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                        .build()
                                    preview.setSurfaceProvider(previewView.surfaceProvider)
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setTargetResolution(
                                            Size(
                                                previewView.width,
                                                previewView.height
                                            )
                                        )
                                        .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                    imageAnalysis.setAnalyzer(
                                        ContextCompat.getMainExecutor(context),
                                        QrCodeAnalyzer { result ->
                                            code = result
                                        }
                                    )
                                    try {
                                        cameraProviderFuture.get().bindToLifecycle(
                                            lifecycleOwner,
                                            selector,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    previewView
                                }, modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = code,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            )
                        }

                    }
                    Box(modifier = Modifier
                        .size(300.dp)
                        .border(width = 3.dp, color = Pink)
                        .constrainAs(box)
                        {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        })
                }
            }
        }
    }
}

