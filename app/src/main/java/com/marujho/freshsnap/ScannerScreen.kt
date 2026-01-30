package com.marujho.freshsnap

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.marujho.freshsnap.ui.openFoodFacts.OpenFoodViewModel
import com.marujho.freshsnap.ui.theme.FreshSnapTheme
import java.util.concurrent.Executors
import android.Manifest
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.marujho.freshsnap.data.model.ScanType
import java.util.regex.Pattern

@Composable
fun BarCodeScanScreen(
    onNavigateToDetail: (String) -> Unit = {}
) {
    Log.d("OFF_TEST2", "Entro")

    val context = LocalContext.current

    //Hay Permiso??
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    //Pedir Permiso
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    if (hasCameraPermission) {
        CameraContent(onNavigateToDetail = onNavigateToDetail)
    } else {
        LaunchedEffect(key1 = true) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CameraContent(
    scanType: ScanType = ScanType.BARCODE,
    onResultScanned: (String) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var scannedBarcode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(scannedBarcode) {
        if (scannedBarcode != null) {
            Log.d("OFF_TEST2", "Código detectado en estado: $scannedBarcode")
            onNavigateToDetail(scannedBarcode!!)
        }
    }

    val barcodeScanner by lazy {
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

    val textScanner by lazy {
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
                    if (scannedBarcode != null) return@MlKitAnalyzer

                    val barcodeResults = result?.getValue(barcodeScanner)
                    if (!barcodeResults.isNullOrEmpty()) {
                        val barcode = barcodeResults.first().rawValue
                        if (barcode != null) {
                            Log.e("BARCODE", "Leído hardware: $barcode")
                            scannedBarcode = barcode
                        }
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
                    if (textResults != null) {
                        val dateFound = findExpirationDate(textResults.text)
                        if (dateFound != null) {
                            onResultScanned(dateFound)
                        }
                    }
                }
            }
        }
        cameraController.setImageAnalysisAnalyzer(cameraExecutor, analyzer)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        cornerRadius,
                        cornerRadius
                    ),
                    blendMode = BlendMode.Clear
                )

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(left, top),
                    size = Size(rectWidth, rectHeight),
                    style = Stroke(width = 4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        cornerRadius,
                        cornerRadius
                    )
                )
            }
        }
    }
}

fun findExpirationDate(text : String) : String ?{

    val patterns = listOf(
        "\\b\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}\\b",
        "\\b\\d{1,2}[\\s.-]+(?:ENE|FEB|MAR|ABR|MAY|JUN|JUL|AGO|SEP|OCT|NOV|DIC|JAN|APR|AUG|DEC)[a-z]*[\\s.-]+\\d{2,4}\\b",
        "\\b\\d{4}[/.-]\\d{1,2}[/.-]\\d{1,2}\\b",
        "\\b(0[1-9]|1[0-2])[/.-]\\d{4}\\b"
    )

    for (pattern in patterns) {
        try {
            val compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
            val matcher = compiledPattern.matcher(text)

            if (matcher.find()) {
                return matcher.group()
            }
        } catch (e: Exception) {
            continue
        }
    }
    return null
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BarCodeScanScreenPreview() {
    FreshSnapTheme {
        CameraContent()
    }
}
