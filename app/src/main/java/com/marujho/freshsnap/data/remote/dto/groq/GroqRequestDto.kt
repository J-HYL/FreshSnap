package com.marujho.freshsnap.data.remote.dto.groq

import com.squareup.moshi.Json

data class GroqRequestDto(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessageDto>,
    @Json(name = "response_format") val responseFormat: GroqResponseFormatDto = GroqResponseFormatDto(),
    val temperature: Float = 0.0f,
    @Json(name = "max_tokens") val maxTokens: Int = 1000
)
