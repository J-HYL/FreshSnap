package com.marujho.freshsnap.data.repository

import com.marujho.freshsnap.data.remote.api.TheMealDBApi
import javax.inject.Inject
import kotlin.collections.emptyList

class RecipeRepository @Inject constructor(
    private val theMealDBApi: TheMealDBApi
) {
    suspend fun getRecipesForIngredient(ingredientName: String){
        try {
            val response = theMealDBApi.getMealByIngredient(ingredientName)

            val meals = response.meals ?: emptyList()

            if (meals.isNotEmpty()) {
                //Crear modelo y vistas para las recetas
            }else{
                //Decir que no hay recetas
            }
        }
        catch (e: Exception){

        }
    }
}