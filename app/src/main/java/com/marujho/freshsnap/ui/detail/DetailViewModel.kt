package com.marujho.freshsnap.ui.detail

import NutrimentsDto
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
import com.marujho.freshsnap.data.remote.dto.NutrientLevel
import com.marujho.freshsnap.data.remote.dto.NutrimentsLevelDto
import com.marujho.freshsnap.data.repository.ProductRepository
import com.marujho.freshsnap.data.repository.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val barcodeDomain: BarcodeDomain,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val barcode: String = checkNotNull(savedStateHandle["barcode"])
    private val passedProductId: String? = savedStateHandle["productId"]


    var uiState by mutableStateOf<DetailUiState>(DetailUiState.Loading)
        private set

    private var currentFirestoreId: String? = null


    private val _allergyMatches = MutableStateFlow<List<String>>(emptyList())
    val allergyMatches: StateFlow<List<String>> = _allergyMatches


    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            uiState = DetailUiState.Loading

            val firebaseResult = productRepository.getProductByEan(barcode)
            val localProduct = firebaseResult.getOrNull()

            if (localProduct != null) {

                if (passedProductId != null && passedProductId == localProduct.id) {
                    currentFirestoreId = localProduct.id
                    localProduct.expirationDate?.let { millis ->
                        setExpirationDateFromMillis(millis)
                    }
                } else {
                    currentFirestoreId = null
                }

                val dto = localProduct.toDto()
                uiState = DetailUiState.Success(dto)

                checkAllergens(dto)

            } else {
                currentFirestoreId = null
                loadFromApi()
            }
        }
    }

    private suspend fun loadFromApi() {
        try {
            val response = barcodeDomain.getProductByBarcode(barcode)

            if (response.status == 1 && response.product != null) {
                uiState = DetailUiState.Success(response.product)
                checkAllergens(response.product)
            } else {
                uiState = DetailUiState.Error("Producto no encontrado")
            }

        } catch (e: Exception) {
            uiState = DetailUiState.Error(e.message ?: "Error de conexión")
        }
    }


    private fun checkAllergens(product: ProductDto) {
        viewModelScope.launch {

            val userAllergies = userPreferences.userAllergies.first()
            val productAllergens = product.allergensTags ?: emptyList()

            val matches = productAllergens.filter { tag ->
                userAllergies.contains(tag)
            }

            _allergyMatches.value = matches
        }
    }


    fun saveProduct(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentState = uiState

        if (currentState is DetailUiState.Success) {
            val dto = currentState.product

            val expirationMillis = expirationDate?.let { dateString ->
                try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .parse(dateString)?.time
                } catch (e: Exception) {
                    null
                }
            } ?: (System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)

            val userProduct = UserProduct(
                id = currentFirestoreId ?: "",
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
                saltLevel = dto.nutrimentsLevels?.salt?.name,
                allergensTags = dto.allergensTags
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

    fun setExpirationDateFromMillis(millis: Long) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        expirationDate = formatter.format(Date(millis))
    }

    fun setExpirationFromScan(date: String) {
        expirationDate = date
    }


    private fun UserProduct.toDto(): ProductDto {
        return ProductDto(
            code = this.ean,
            productName = this.name,
            brands = this.brand,
            imageUrl = this.imageUrl,
            quantity = this.quantity,
            categories = this.categories,
            packaging = this.packaging,
            countries = this.countries,
            nutriScore = this.nutriScore,
            novaGroup = this.novaGroup,
            greenScore = this.greenScore,
            allergensTags = this.allergensTags ?: emptyList(),
            nutriments = NutrimentsDto(
                energyKcal100g = this.energyKcal,
                energyKj100g = this.energyKj,
                fat100g = this.fat,
                saturatedFat100g = this.saturatedFat,
                carbohydrates100g = this.carbohydrates,
                sugars100g = this.sugars,
                proteins100g = this.proteins,
                salt100g = this.salt,
                fiber100g = this.fiber,
                sodium100g = this.sodium
            ),
            nutrimentsLevels = NutrimentsLevelDto(
                fat = getNutrientLevel(this.fatLevel),
                saturatedFat = getNutrientLevel(this.saturatedFatLevel),
                sugars = getNutrientLevel(this.sugarLevel),
                salt = getNutrientLevel(this.saltLevel)
            )
        )
    }
}


sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val product: ProductDto) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

private fun getNutrientLevel(level: String?): NutrientLevel? {
    return try {
        level?.let { NutrientLevel.valueOf(it) }
    } catch (e: IllegalArgumentException) {
        null
    }
}