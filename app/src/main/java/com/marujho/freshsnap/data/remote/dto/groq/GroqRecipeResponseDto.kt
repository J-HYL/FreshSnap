package com.marujho.freshsnap.data.remote.dto.groq

import com.squareup.moshi.Json

data class GroqRecipeResponseDto(
    @Json(name = "recipe_id") val recipeId: String? = null,
    val title: String = "",
    val instructions: String? = null,
    @Json(name = "ingredientes_tengo") val ingredientesTengo: List<GroqIngredientDto> = emptyList(),
    @Json(name = "ingredientes_falta") val ingredientesFalta: List<GroqIngredientDto> = emptyList(),
    val category: String? = null,
    val area: String? = null,
    val reason: String? = null
)
