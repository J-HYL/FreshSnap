package com.marujho.freshsnap.ui.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.domain.BarcodeDomain
import com.marujho.freshsnap.data.model.UserProduct
import com.marujho.freshsnap.data.remote.dto.ProductDto
import com.marujho.freshsnap.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val barcodeDomain: BarcodeDomain,
    private val productRepository: ProductRepository,
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
                } else {
                    uiState = DetailUiState.Error("Producto no encontrado")
                }
            } catch (e: Exception) {
                uiState = DetailUiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    fun saveProduct(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentState = uiState
        if (currentState is DetailUiState.Success) {
            val dto = currentState.product

            val expirationMillis = expirationDate?.let { dateString ->
                try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateString)?.time
                } catch (e: Exception) {
                    null
                }
            } ?: (System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000))

            // API -> Firebase
            val userProduct = UserProduct(
                ean = barcode,
                name = dto.productName ?: "Producto sin nombre",
                brand = dto.brands ?: "Marca desconocida",
                imageUrl = dto.imageUrl,
                quantity = dto.quantity,
                categories = dto.categories,
                packaging = dto.packaging,
                countries = dto.countries,

                nutriScore = dto.nutriScore?.uppercase(),
                novaGroup = dto.novaGroup,
                greenScore = dto.greenScore?.uppercase(),

                scanDate = System.currentTimeMillis(),
                expirationDate = expirationMillis,

                energyKcal = dto.nutriments?.energyKcal100g,
                energyKj = dto.nutriments?.energyKj100g,
                fat = dto.nutriments?.fat100g,
                saturatedFat = dto.nutriments?.saturatedFat100g,
                carbohydrates = dto.nutriments?.carbohydrates100g,
                sugars = dto.nutriments?.sugars100g,
                proteins = dto.nutriments?.proteins100g,
                salt = dto.nutriments?.salt100g,
                fiber = dto.nutriments?.fiber100g,
                sodium = dto.nutriments?.sodium100g,

                fatLevel = dto.nutrimentsLevels?.fat?.name,
                saturatedFatLevel = dto.nutrimentsLevels?.saturatedFat?.name,
                sugarLevel = dto.nutrimentsLevels?.sugars?.name,
                saltLevel = dto.nutrimentsLevels?.salt?.name
            )

            viewModelScope.launch {
                val result = productRepository.saveProduct(userProduct)
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Error al guardar")
                }
            }
        }
    }

    var expirationDate by mutableStateOf<String?>(null)
        private set

    fun setExpirationDatefromCal(cal: Long?) {
        if (cal != null) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            expirationDate = formatter.format(Date(cal))
        }
    }
    fun setExpirationFromScan(date: String) {
        expirationDate = date
    }
}

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val product: ProductDto) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}