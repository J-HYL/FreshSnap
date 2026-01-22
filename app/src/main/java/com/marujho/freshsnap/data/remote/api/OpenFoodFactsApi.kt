package com.marujho.freshsnap.data.remote.api

import com.marujho.freshsnap.data.remote.dto.ProductResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): ProductResponseDto
}