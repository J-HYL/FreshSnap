package com.marujho.freshsnap.ui.recipe

import com.marujho.freshsnap.data.model.CachedRecipe

sealed class RecipeUiState {
    object Loading : RecipeUiState()
    data class Success(val recipes: List<CachedRecipe>) : RecipeUiState()
    object EmptyNoRed : RecipeUiState()
    object EmptyNoProducts : RecipeUiState()
    data class Error(val message: String) : RecipeUiState()
}
