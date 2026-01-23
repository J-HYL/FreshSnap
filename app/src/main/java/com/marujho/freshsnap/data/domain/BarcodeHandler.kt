package com.marujho.freshsnap.data.domain

import com.marujho.freshsnap.data.remote.api.OpenFoodFactsApi
import com.marujho.freshsnap.data.remote.dto.ProductResponseDto
import javax.inject.Inject

class BarcodeDomain @Inject constructor(
    private val api: OpenFoodFactsApi
) {

    suspend fun getProductByBarcode(barcode: String): ProductResponseDto {
        return api.getProductByBarcode(barcode)
    }
}
