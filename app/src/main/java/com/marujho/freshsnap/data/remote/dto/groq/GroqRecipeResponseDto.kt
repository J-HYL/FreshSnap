package com.marujho.freshsnap.data.remote.dto.groq

import com.squareup.moshi.Json

data class GroqRecipeResponseDto(
    @Json(name = "is_possible") val isPossible: Boolean? = true,
    val message: String? = null,
    val title: String? = null,
    val instructions: String? = null,
    @Json(name = "ingredientes_tengo") val ingredientesTengo: List<GroqIngredientDto>? = emptyList(),
    @Json(name = "ingredientes_falta") val ingredientesFalta: List<GroqIngredientDto>? = emptyList()
)