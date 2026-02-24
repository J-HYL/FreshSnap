package com.marujho.freshsnap.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.marujho.freshsnap.R
import com.marujho.freshsnap.data.model.ScanType
import java.util.concurrent.Executors

@Composable
fun BarCodeScanScreen(
    viewModel: ScannerViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit = {},
    onDateScanned: (String) -> Unit = {},
    scanType: ScanType = ScanType.BARCODE,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val scannedResult by viewModel.scannedResult.collectAsState()


    LaunchedEffect(scannedResult) {
        scannedResult?.let { result ->
            if (scanType == ScanType.BARCODE) {
                onNavigateToDetail(result)
            } else {
                onDateScanned(result)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetScanState()
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraContent(
            viewModel = viewModel,
            scanType = scanType,
            onBackClick = onBackClick
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CameraContent(
    viewModel: ScannerViewModel,
    scanType: ScanType,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val isFlashOn by viewModel.isFlashOn.collectAsState()

    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    val textScanner = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    LaunchedEffect(scanType) {
        val analyzer = when (scanType) {
            ScanType.BARCODE -> {
                MlKitAnalyzer(
                    listOf(barcodeScanner),
                    ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    cameraExecutor
                ) { result: MlKitAnalyzer.Result? ->
                    val barcodeResults = result?.getValue(barcodeScanner)
                    if (!barcodeResults.isNullOrEmpty()) {
                        viewModel.processBarcode(barcodeResults.first().rawValue)
                    }
                }
            }
            ScanType.EX_DATE -> {
                MlKitAnalyzer(
                    listOf(textScanner),
                    ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                    cameraExecutor
                ) { result: MlKitAnalyzer.Result? ->
                    val textResults = result?.getValue(textScanner)
                    viewModel.processText(textResults?.text)
                }
            }
        }
        cameraController.setImageAnalysisAnalyzer(cameraExecutor, analyzer)
    }

    LaunchedEffect(isFlashOn) {
        cameraController.enableTorch(isFlashOn)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        scaleType = PreviewView.ScaleType.FILL_START
                        controller = cameraController
                    }
                },
                update = {
                    cameraController.bindToLifecycle(lifeCycleOwner)
                }
            )

            ScannerOverlay()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .zIndex(10f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button_desc),
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = { viewModel.toggleFlash() },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = if (isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off),
                        contentDescription = stringResource(R.string.flash_button_desc),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val rectWidth = width * 0.75f
        val rectHeight = height * 0.25f

        val left = (width - rectWidth) / 2
        val top = (height - rectHeight) / 2
        val cornerRadius = 20f

        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size
        )

        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(rectWidth, rectHeight),
            style = Stroke(width = 4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )
    }
}