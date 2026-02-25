package com.marujho.freshsnap.ui.scanner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor() : ViewModel() {

    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()

    // ESTADO NUEVO: Aquí guardaremos lo que la cámara lea
    private val _scannedResult = MutableStateFlow<String?>(null)
    val scannedResult: StateFlow<String?> = _scannedResult.asStateFlow()

    private var hasProcessedResult = false

    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }

    fun resetScanState() {
        hasProcessedResult = false
        _scannedResult.value = null // Reseteamos también el resultado
    }

    // Ya no pasamos la lambda (onResult), solo el barcode
    fun processBarcode(barcode: String?) {
        if (barcode != null && !hasProcessedResult) {
            hasProcessedResult = true
            _scannedResult.value = barcode // Actualizamos el estado
        }
    }

    // Ya no pasamos la lambda, solo el texto
    fun processText(text: String?) {
        if (text != null && !hasProcessedResult) {
            val dateFound = findExpirationDate(text)
            if (dateFound != null) {
                hasProcessedResult = true
                _scannedResult.value = dateFound // Actualizamos el estado
            }
        }
    }

    private fun findExpirationDate(text: String): String? {
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
}