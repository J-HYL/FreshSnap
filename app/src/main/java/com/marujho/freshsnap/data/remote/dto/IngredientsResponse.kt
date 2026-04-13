package com.marujho.freshsnap.data.remote.dto

import com.squareup.moshi.Json

data class IngredientsResponse(
    @Json(name = "meals")
    val ingredients: List<IngredientDto>
)

data class IngredientDto(
    val idIngredient: String,
    val strIngredient: String,
    val strDescription: String?,
    val strType: String?
)
