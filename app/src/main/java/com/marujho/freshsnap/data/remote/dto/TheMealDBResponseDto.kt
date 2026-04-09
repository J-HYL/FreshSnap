package com.marujho.freshsnap.data.remote.dto

import com.squareup.moshi.Json

data class TheMealDBResponseDto(
    @Json(name = "meals")
    val meals: List<MealDto>?
)
data class MealDto(
    @Json(name = "idMeal")
    val id: String,
    @Json(name = "strMeal")
    val name: String,
    @Json(name = "strMealThumb")
    val imageUrl: String
)
