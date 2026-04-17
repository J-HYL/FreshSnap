package com.marujho.freshsnap.data.remote.dto.groq

import com.squareup.moshi.Json

data class GroqRequestDto(
    val model: String = "llama3-8b-8192",
    val messages: List<GroqMessageDto>,
    @Json(name = "response_format") val responseFormat: GroqResponseFormatDto = GroqResponseFormatDto(),
    val temperature: Float = 0.3f,
    @Json(name = "max_tokens") val maxTokens: Int = 500
)
