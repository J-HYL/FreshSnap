package com.marujho.freshsnap.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.domain.usecase.ProcessScannedProductUseCase
import com.marujho.freshsnap.data.model.UserProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(private val processScannedProductUseCase: ProcessScannedProductUseCase) : ViewModel() {

    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var hasProcessedResult = false

    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }

    fun resetScanState() {
        hasProcessedResult = false
        _uiState.value = ScannerUiState.Idle
    }

    fun processBarcode(barcode: String?) {
        if (barcode != null && !hasProcessedResult) {
            hasProcessedResult = true

            viewModelScope.launch {
                _uiState.value = ScannerUiState.Loading

                val result = processScannedProductUseCase(barcode)

                if (result.isSuccess) {
                    val productoGuardado = result.getOrNull()!!
                    _uiState.value = ScannerUiState.Success(productoGuardado)
                } else {
                    val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                    _uiState.value = ScannerUiState.Error(errorMsg)
                }
            }
        }
    }

    fun processText(text: String?) {
        if (text != null && !hasProcessedResult) {
            val dateFound = findExpirationDate(text)
            if (dateFound != null) {
                hasProcessedResult = true
                _uiState.value = ScannerUiState.DateFound(dateFound)
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
sealed interface ScannerUiState {
    object Idle : ScannerUiState
    object Loading : ScannerUiState
    data class Success(val product: UserProduct) : ScannerUiState
    data class Error(val message: String) : ScannerUiState
    data class DateFound(val date: String) : ScannerUiState
}