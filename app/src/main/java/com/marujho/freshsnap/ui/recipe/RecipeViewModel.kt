package com.marujho.freshsnap.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.model.RecipeIngredient
import com.marujho.freshsnap.data.repository.ProductRepository
import com.marujho.freshsnap.data.repository.RecipeRepository
import com.marujho.freshsnap.data.repository.ShoppingRepository
import com.marujho.freshsnap.data.repository.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val shoppingRepository: ShoppingRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading

            val productsResult = productRepository.getAllProducts()
            if (productsResult.isFailure) {
                _uiState.value = RecipeUiState.Error(
                    productsResult.exceptionOrNull()?.message ?: "Error desconocido"
                )
                return@launch
            }

            val products = productsResult.getOrThrow()
            if (products.isEmpty()) {
                _uiState.value = RecipeUiState.EmptyNoProducts
                return@launch
            }

            val redDays = userPreferences.expiryRedDays.first()
            val yellowDays = userPreferences.expiryYellowDays.first()

            val result = recipeRepository.getRecipeSuggestions(
                products = products,
                redDays = redDays,
                yellowDays = yellowDays,
                forceRefresh = forceRefresh
            )

            if (result.isSuccess) {
                val recipes = result.getOrThrow()
                _uiState.value = if (recipes.isEmpty()) {
                    RecipeUiState.EmptyNoRed
                } else {
                    RecipeUiState.Success(recipes)
                }
            } else {
                _uiState.value = RecipeUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al buscar recetas"
                )
            }
        }
    }

    fun addMissingToShoppingList(ingredients: List<RecipeIngredient>) {
        viewModelScope.launch {
            ingredients.forEach { ingredient ->
                val itemName = if (ingredient.measure.isNotBlank()) {
                    "${ingredient.name} (${ingredient.measure})"
                } else {
                    ingredient.name
                }
                shoppingRepository.addShoppingItem(itemName)
            }
            _snackbarMessage.value = "added"
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
