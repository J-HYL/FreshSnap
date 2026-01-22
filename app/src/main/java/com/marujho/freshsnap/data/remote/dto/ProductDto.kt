package com.marujho.freshsnap.data.remote.dto
//representa el objeto product dentro de la respuesta
import com.squareup.moshi.Json

data class ProductDto(
    @Json(name = "product_name")
    val productName: String?,

    val brands: String?,

    @Json(name = "image_front_url")
    val imageUrl: String?
)