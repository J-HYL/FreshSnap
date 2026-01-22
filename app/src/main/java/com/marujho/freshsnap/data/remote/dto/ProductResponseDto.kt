package com.marujho.freshsnap.data.remote.dto

//representa la respuesta completa de la API
data class ProductResponseDto(
    val status: Int,
    val product: ProductDto?
)