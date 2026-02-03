package com.marujho.freshsnap.data.remote.dto

import com.squareup.moshi.Json

enum class NutrientLevel {
    @Json(name = "low")
    LOW,
    @Json(name = "moderate")
    MODERATE,
    @Json(name = "high")
    HIGH
}

data class NutrimentsLevelDto(
    @Json(name = "fat")
    val fat: NutrientLevel?,

    @Json(name = "saturated-fat")
    val saturatedFat: NutrientLevel?,

    @Json(name = "sugars")
    val sugars: NutrientLevel?,

    @Json(name = "salt")
    val salt: NutrientLevel?
)
