package com.marujho.freshsnap.ui.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.domain.BarcodeDomain
import com.marujho.freshsnap.data.remote.dto.ProductDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val barcodeDomain: BarcodeDomain,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val barcode: String = checkNotNull(savedStateHandle["barcode"])

    var uiState by mutableStateOf<DetailUiState>(DetailUiState.Loading)
        private set

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            try {
                val response = barcodeDomain.getProductByBarcode(barcode)
                if (response.status == 1 && response.product != null) {
                    uiState = DetailUiState.Success(response.product)
                    Log.d("OFF_TEST2", "Status: ${response.status},ENCONTRADO")

                } else {
                    uiState = DetailUiState.Error("Producto no encontrado")
                    Log.d("OFF_TEST2", "Status: ${response.status},NO ENCONTRADO")
                }
            } catch (e: Exception) {
                uiState = DetailUiState.Error(e.message ?: "Error desconocido")
                Log.e("OFF_TEST2", "Error en dominio", e)
            }
        }
    }
}

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val product: ProductDto) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}