package com.marujho.freshsnap.data.repository

import androidx.compose.foundation.isSystemInDarkTheme
import com.marujho.freshsnap.data.remote.api.TheMealDBApi
import com.marujho.freshsnap.data.remote.dto.MealDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlin.collections.emptyList

class RecipeRepository @Inject constructor(
    private val theMealDBApi: TheMealDBApi
) {
    suspend fun getRecipesForIngredients(ingredients: List<String>): List<MealDto> {
        return coroutineScope {
            val deferredResponses = ingredients.map{ ingredient ->
                async {
                    try {
                        val response = theMealDBApi.getMealByIngredient(ingredient)
                        if (response.isSuccessful){
                            response.body()?.meals ?: emptyList()
                        } else {
                            emptyList()

                        }
                    } catch (e : Exception){
                        emptyList()
                    }
                }
            }
            val allMealsFound: List<MealDto> = deferredResponses.awaitAll().flatten()

            val rankedMeals = allMealsFound.groupBy { it.id }.map { (id,duplicateMealsList)->
                Pair(duplicateMealsList.first(),duplicateMealsList.size)
            }.sortedByDescending { it.second }.map { it.first }
            return@coroutineScope rankedMeals
        }
    }
}