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
            // Formato clásico: DD/MM/YYYY, DD/MM/YY, DD.MM.YYYY, DD MM YY (ej: 21/06/26, 05-12-2024)
            "\\b(?:3[01]|[12][0-9]|0?[1-9])[\\s/.-]+(?:1[0-2]|0?[1-9])[\\s/.-]+(?:20\\d{2}|\\d{2})\\b",

            // Formato inverso: YYYY/MM/DD, YYYY.MM.DD (ej: 2024.05.12)
            "\\b(?:20\\d{2})[\\s/.-]+(?:1[0-2]|0?[1-9])[\\s/.-]+(?:3[01]|[12][0-9]|0?[1-9])\\b",

            // Meses en texto: DD MES YYYY, DD MES YY (ej: 12 ENE 2025, 12 ABR 26)
            "\\b(?:3[01]|[12][0-9]|0?[1-9])?[\\s/.-]*(?:ENE|FEB|MAR|ABR|MAY|JUN|JUL|AGO|SEP|OCT|NOV|DIC|JAN|APR|AUG|DEC)[A-Z]*[\\s/.-]+(?:20\\d{2}|\\d{2})\\b",

            // Solo mes y año: MM/YYYY, MM/YY (ej: 05/25, 11-2024) -> Estricto del 01 al 12
            "\\b(?:1[0-2]|0[1-9])[\\s/.-]+(?:20\\d{2}|\\d{2})\\b"
        )

        for (pattern in patterns) {
            try {
                val compiledPattern = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE)
                val matcher = compiledPattern.matcher(text)

                if (matcher.find()) {
                    val rawDate = matcher.group()
                    return normalizeDate(rawDate)
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun normalizeDate(rawDate: String): String {
        var clean = rawDate.uppercase().replace(Regex("[\\s.-]+"), "/")

        val monthMap = mapOf(
            "ENE" to "01", "JAN" to "01", "FEB" to "02", "MAR" to "03",
            "ABR" to "04", "APR" to "04", "MAY" to "05", "JUN" to "06",
            "JUL" to "07", "AGO" to "08", "AUG" to "08", "SEP" to "09",
            "OCT" to "10", "NOV" to "11", "DIC" to "12", "DEC" to "12"
        )
        for ((key, value) in monthMap) {
            if (clean.contains(key)) {
                clean = clean.replace(Regex("[A-Z]*$key[A-Z]*"), value)
            }
        }

        clean = clean.replace(Regex("[^0-9/]"), "")

        val parts = clean.split("/").filter { it.isNotEmpty() }

        try {
            if (parts.size == 3) {
                val p0 = parts[0]
                val p1 = parts[1].padStart(2, '0')
                val p2 = parts[2]

                var day: String
                var month: String
                var year: String

                if (p0.length == 4) {
                    year = p0
                    month = p1
                    day = p2.padStart(2, '0')
                } else {
                    day = p0.padStart(2, '0')
                    month = p1
                    year = p2
                }

                if (year.length == 2) {
                    year = "20$year"
                }

                return "$day/$month/$year"

            } else if (parts.size == 2) {
                val month = parts[0].padStart(2, '0')
                var year = parts[1]

                if (year.length == 2) {
                    year = "20$year"
                }

                return "01/$month/$year"
            }
        } catch (e: Exception) {
            return clean
        }

        return clean
    }
}