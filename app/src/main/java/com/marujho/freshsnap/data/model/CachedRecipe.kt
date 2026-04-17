package com.marujho.freshsnap.data.model

data class CachedRecipe(
    val id: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val instructions: String = "",
    val ingredientsOwned: List<Map<String, String>> = emptyList(),
    val ingredientsMissing: List<Map<String, String>> = emptyList(),
    val source: String = "",
    val generatedAt: Long = 0L,
    val inventoryHash: String = ""
) {
    fun toRecipeIngredients(list: List<Map<String, String>>): List<RecipeIngredient> =
        list.map { map ->
            RecipeIngredient(
                name = map["name"] ?: "",
                measure = map["measure"] ?: ""
            )
        }

    companion object {
        fun fromIngredients(ingredients: List<RecipeIngredient>): List<Map<String, String>> =
            ingredients.map { mapOf("name" to it.name, "measure" to it.measure) }
    }
}
