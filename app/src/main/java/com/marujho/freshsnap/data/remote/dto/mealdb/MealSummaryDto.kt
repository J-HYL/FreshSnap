package com.marujho.freshsnap.data.remote.dto.mealdb

import com.squareup.moshi.Json

data class MealSummaryDto(
    @Json(name = "strMeal") val name: String?,
    @Json(name = "strMealThumb") val thumbnailUrl: String?,
    @Json(name = "idMeal") val id: String?
)
