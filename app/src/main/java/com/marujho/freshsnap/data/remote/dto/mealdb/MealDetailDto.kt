package com.marujho.freshsnap.data.remote.dto.mealdb

import com.squareup.moshi.Json

data class MealDetailDto(
    @Json(name = "idMeal") val id: String?,
    @Json(name = "strMeal") val name: String?,
    @Json(name = "strCategory") val category: String?,
    @Json(name = "strArea") val area: String?,
    @Json(name = "strInstructions") val instructions: String?,
    @Json(name = "strMealThumb") val thumbnailUrl: String?,

    @Json(name = "strIngredient1") val ingredient1: String?,
    @Json(name = "strIngredient2") val ingredient2: String?,
    @Json(name = "strIngredient3") val ingredient3: String?,
    @Json(name = "strIngredient4") val ingredient4: String?,
    @Json(name = "strIngredient5") val ingredient5: String?,
    @Json(name = "strIngredient6") val ingredient6: String?,
    @Json(name = "strIngredient7") val ingredient7: String?,
    @Json(name = "strIngredient8") val ingredient8: String?,
    @Json(name = "strIngredient9") val ingredient9: String?,
    @Json(name = "strIngredient10") val ingredient10: String?,
    @Json(name = "strIngredient11") val ingredient11: String?,
    @Json(name = "strIngredient12") val ingredient12: String?,
    @Json(name = "strIngredient13") val ingredient13: String?,
    @Json(name = "strIngredient14") val ingredient14: String?,
    @Json(name = "strIngredient15") val ingredient15: String?,
    @Json(name = "strIngredient16") val ingredient16: String?,
    @Json(name = "strIngredient17") val ingredient17: String?,
    @Json(name = "strIngredient18") val ingredient18: String?,
    @Json(name = "strIngredient19") val ingredient19: String?,
    @Json(name = "strIngredient20") val ingredient20: String?,

    @Json(name = "strMeasure1") val measure1: String?,
    @Json(name = "strMeasure2") val measure2: String?,
    @Json(name = "strMeasure3") val measure3: String?,
    @Json(name = "strMeasure4") val measure4: String?,
    @Json(name = "strMeasure5") val measure5: String?,
    @Json(name = "strMeasure6") val measure6: String?,
    @Json(name = "strMeasure7") val measure7: String?,
    @Json(name = "strMeasure8") val measure8: String?,
    @Json(name = "strMeasure9") val measure9: String?,
    @Json(name = "strMeasure10") val measure10: String?,
    @Json(name = "strMeasure11") val measure11: String?,
    @Json(name = "strMeasure12") val measure12: String?,
    @Json(name = "strMeasure13") val measure13: String?,
    @Json(name = "strMeasure14") val measure14: String?,
    @Json(name = "strMeasure15") val measure15: String?,
    @Json(name = "strMeasure16") val measure16: String?,
    @Json(name = "strMeasure17") val measure17: String?,
    @Json(name = "strMeasure18") val measure18: String?,
    @Json(name = "strMeasure19") val measure19: String?,
    @Json(name = "strMeasure20") val measure20: String?
) {
    fun getIngredientsList(): List<Pair<String, String>> {
        val ingredients = listOf(
            ingredient1, ingredient2, ingredient3, ingredient4, ingredient5,
            ingredient6, ingredient7, ingredient8, ingredient9, ingredient10,
            ingredient11, ingredient12, ingredient13, ingredient14, ingredient15,
            ingredient16, ingredient17, ingredient18, ingredient19, ingredient20
        )
        val measures = listOf(
            measure1, measure2, measure3, measure4, measure5,
            measure6, measure7, measure8, measure9, measure10,
            measure11, measure12, measure13, measure14, measure15,
            measure16, measure17, measure18, measure19, measure20
        )
        return ingredients.zip(measures).filter { (ing, _) ->
            !ing.isNullOrBlank()
        }.map { (ing, meas) ->
            (ing ?: "") to (meas?.trim() ?: "")
        }
    }
}
