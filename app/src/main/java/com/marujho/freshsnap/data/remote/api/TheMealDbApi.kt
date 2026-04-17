package com.marujho.freshsnap.data.remote.api

import com.marujho.freshsnap.data.remote.dto.mealdb.MealFilterResponseDto
import com.marujho.freshsnap.data.remote.dto.mealdb.MealLookupResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApi {

    @GET("api/json/v1/1/filter.php")
    suspend fun searchByIngredient(
        @Query("i") ingredient: String
    ): MealFilterResponseDto

    @GET("api/json/v1/1/lookup.php")
    suspend fun getMealById(
        @Query("i") mealId: String
    ): MealLookupResponseDto
}
