package com.marujho.freshsnap.data.remote.api

import com.marujho.freshsnap.data.remote.dto.TheMealDBResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDBApi {
    @GET("filter.php")
    suspend fun getMealByIngredient(
        @Query("i") ingredient: String
    ): TheMealDBResponseDto
}