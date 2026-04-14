package com.marujho.freshsnap.data.domain.usecase

import android.util.Log
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import com.marujho.freshsnap.data.model.UserProduct
import com.marujho.freshsnap.data.remote.api.OpenFoodFactsApi
import com.marujho.freshsnap.data.remote.api.TheMealDBApi
import com.marujho.freshsnap.data.repository.IngredientRepository
import com.marujho.freshsnap.data.repository.ProductRepository
import javax.inject.Inject

class ProcessScannedProductUseCase @Inject constructor(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val ingredientRepository: IngredientRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): Result<UserProduct> {
        return try {
            val offResponse = openFoodFactsApi.getProductByBarcode(barcode)
            val productDto = offResponse.product ?: throw Exception("Producto no encontrado en la base de datos.")

            val validIngredients = ingredientRepository.getValidIngredients()

            if (validIngredients.isEmpty()){
                Log.e("PRUEBA_CRUCE", "Vacia la lista ", )
            }

            val matchedIng = productDto.ingredientsTags?.mapNotNull { tag ->
            val cleanTag = tag.replace("en:","").replace("-"," ").lowercase()
            if (validIngredients.contains(cleanTag)) cleanTag else null
            }?.distinct() ?: emptyList()

            Log.d("INGREDIENTES_Match", matchedIng.toString())


            val userProduct = UserProduct(
                ean = barcode,
                name = productDto.productName ?: "Producto desconocido",
                recipeIngredients = matchedIng,
                brand = productDto.brands ?: "Desconocida",
                imageUrl = productDto.imageUrl,
                quantity = productDto.quantity,
                categories = productDto.categories,
                packaging = productDto.packaging,
                countries = productDto.countries,
                nutriScore = productDto.nutriScore,
                novaGroup = productDto.novaGroup,
                greenScore = productDto.greenScore,
                energyKcal = productDto.nutriments?.energyKcal100g,
                energyKj = productDto.nutriments?.energyKj100g,
                fat = productDto.nutriments?.fat100g,
                saturatedFat = productDto.nutriments?.saturatedFat100g,
                carbohydrates = productDto.nutriments?.carbohydrates100g,
                sugars = productDto.nutriments?.sugars100g,
                proteins = productDto.nutriments?.proteins100g,
                salt = productDto.nutriments?.salt100g,
                fiber = productDto.nutriments?.fiber100g,
                sodium = productDto.nutriments?.sodium100g,
                fatLevel = productDto.nutrimentsLevels?.fat?.name,
                saturatedFatLevel = productDto.nutrimentsLevels?.saturatedFat?.name,
                sugarLevel = productDto.nutrimentsLevels?.sugars?.name,
                saltLevel = productDto.nutrimentsLevels?.salt?.name,
                allergensTags = productDto.allergensTags
            )

            val saveREsult = productRepository.saveProduct(userProduct)
            if (saveREsult.isSuccess){
                Result.success(userProduct)
            } else {
                Result.failure(saveREsult.exceptionOrNull() ?: Exception("Error al guardar el producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}