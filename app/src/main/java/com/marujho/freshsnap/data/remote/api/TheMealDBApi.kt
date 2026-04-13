package com.marujho.freshsnap.data.remote.api

import com.marujho.freshsnap.data.remote.dto.IngredientsResponse
import com.marujho.freshsnap.data.remote.dto.TheMealDBResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDBApi {
    @GET("filter.php")
    suspend fun getMealByIngredient(
        @Query("i") ingredient: String
    ): Response<TheMealDBResponseDto>

    @GET("list.php?i=list")
    suspend fun getAlIngredients(): Response<IngredientsResponse>
}