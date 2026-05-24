package com.marujho.freshsnap.ui.recipe

import com.marujho.freshsnap.data.model.CachedRecipe

sealed class RecipeUiState {
    object LoadingInventory : RecipeUiState()
    data class Ready(
        val ingredients: List<IngredientSelection>,
        val isGenerating: Boolean = false,
        val recipe: CachedRecipe? = null,
        val errorMessage: String? = null
    ) : RecipeUiState()
}
