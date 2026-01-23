package com.marujho.freshsnap

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.marujho.freshsnap.ui.theme.FreshSnapTheme
import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.marujho.freshsnap.ui.theme.LightGreen
import java.util.concurrent.Executors
import com.google.mlkit.vision.barcode.*
import com.google.mlkit.vision.barcode.common.Barcode



class ScannerScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreshSnapTheme {
                BarCodeScanScreen()
            }
        }
    }
}

@Composable
fun BarCodeScanScreen() {
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
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    if (hasCameraPermission) {
        Text(text = "Camera permission granted")
        CameraContent()
    } else {
        LaunchedEffect(key1 = true) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CameraContent() {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val cameraExecutor =
        remember { Executors.newSingleThreadExecutor() } //Creamos el hilo para analizar el codigo de barras
    val options =
        BarcodeScannerOptions.Builder() //Vemos los codigos de barras que queremos captar, en este caso son 4 que se suelen usar para la comida
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
    var barcodeScanner = remember {
        BarcodeScanning.getClient(options)
    }
    LaunchedEffect(cameraController, cameraExecutor) {
        cameraController.setImageAnalysisAnalyzer(
            cameraExecutor,
            MlKitAnalyzer(
                listOf(barcodeScanner),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                cameraExecutor
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                if (barcodeResults?.isNotEmpty() == true) {
                    val barcode = barcodeResults.first().rawValue
                    if (barcode != null) {
                        Log.d("BARCODE", barcode)
                    }
                }
            })
    }


                    Scaffold (modifier = Modifier.fillMaxSize()) { paddingValues: PaddingValues ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    factory = { context ->
                        PreviewView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                            scaleType = PreviewView.ScaleType.FILL_START
                        }.also { previewView ->
                            previewView.controller = cameraController
                            cameraController.bindToLifecycle(lifeCycleOwner)
                        }

                    }
                )
            }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun BarCodeScanScreenPreview() {
        CameraContent()
        /*FreshSnapTheme {
            BarCodeScanScreen()
        }*/
    }