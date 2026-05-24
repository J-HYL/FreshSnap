package com.marujho.freshsnap.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marujho.freshsnap.data.domain.IngredientMasticator
import com.marujho.freshsnap.data.model.CachedRecipe
import com.marujho.freshsnap.data.model.RecipeIngredient
import com.marujho.freshsnap.data.model.ShoppingItem
import com.marujho.freshsnap.data.repository.ProductRepository
import com.marujho.freshsnap.data.repository.RecipeRepository
import com.marujho.freshsnap.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IngredientSelection(val name: String, val isSelected: Boolean = false)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val productRepository: ProductRepository,
    private val shoppingRepository: ShoppingRepository,
    private val masticator: IngredientMasticator
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.LoadingInventory)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadInventory()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.LoadingInventory
            val productsResult = productRepository.getAllProducts()

            if (productsResult.isSuccess) {
                val today = System.currentTimeMillis()

                val uniqueIngredients = productsResult.getOrThrow()
                    .filter { product ->
                        val expDate = product.expirationDate ?: today
                        !product.isConsumed && expDate >= today
                    }
                    .map { masticator.extractIngredientName(it) }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                    .map { IngredientSelection(it, isSelected = false) }

                _uiState.value = RecipeUiState.Ready(ingredients = uniqueIngredients)
            } else {
                _uiState.value = RecipeUiState.Ready(
                    ingredients = emptyList(),
                    errorMessage = "Error al cargar tu despensa."
                )
            }
        }
    }

    fun toggleIngredient(name: String) {
        val currentState = _uiState.value
        if (currentState is RecipeUiState.Ready) {
            val updatedList = currentState.ingredients.map {
                if (it.name == name) it.copy(isSelected = !it.isSelected) else it
            }
            _uiState.value = currentState.copy(ingredients = updatedList, errorMessage = null)
        }
    }

    fun generateRecipe() {
        val currentState = _uiState.value
        if (currentState !is RecipeUiState.Ready) return

        val selected = currentState.ingredients.filter { it.isSelected }.map { it.name }
        if (selected.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "Por favor, selecciona al menos un ingrediente.")
            return
        }

        val available = currentState.ingredients.filter { !it.isSelected }.map { it.name }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isGenerating = true, errorMessage = null, recipe = null)

            val result = recipeRepository.generateCustomRecipe(
                selectedIngredients = selected,
                availableIngredients = available
            )

            if (result.isSuccess) {
                _uiState.update {
                    (it as RecipeUiState.Ready).copy(isGenerating = false, recipe = result.getOrThrow())
                }
            } else {
                _uiState.update {
                    (it as RecipeUiState.Ready).copy(
                        isGenerating = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al generar receta."
                    )
                }
            }
        }
    }

    fun addMissingToShoppingList(ingredients: List<RecipeIngredient>) {
        viewModelScope.launch {
            ingredients.forEach { ingredient ->
                shoppingRepository.addShoppingItem(
                    name = ingredient.name,
                    quantity = ingredient.measure,
                    source = ShoppingItem.SOURCE_RECIPE
                )
            }
            _snackbarMessage.value = "Ingredientes añadidos a la lista de la compra"
        }
    }

    fun clearSnackbar() { _snackbarMessage.value = null }
}