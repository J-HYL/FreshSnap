package com.marujho.freshsnap.data.repository

import android.util.Log
import com.marujho.freshsnap.data.remote.api.TheMealDBApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngredientRepository @Inject constructor(
    private val theMealDBApi: TheMealDBApi
) {
    private var cacheIngredients: Set<String>? = null

    suspend fun getValidIngredients(): Set<String> {
        cacheIngredients?.let {
            Log.d("PRUEBA_CACHE", "Usando memoria cache: ")
            return it
        }
        return try {
            Log.d("PRUEBA_CACHE", "Descargando: ")
            val response = theMealDBApi.getAlIngredients()
            val masterIngredientsList = response.body()?.ingredients
                ?: throw  Exception("Error al conectar.")
            val processedSet = masterIngredientsList.map { it.strIngredient.lowercase() }.toSet()

        cacheIngredients = processedSet
        processedSet
        } catch (e: Exception) {
            emptySet()
        }
    }
}