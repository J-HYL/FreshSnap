package com.marujho.freshsnap.data.remote.api

import com.marujho.freshsnap.data.remote.dto.groq.GroqRequestDto
import com.marujho.freshsnap.data.remote.dto.groq.GroqResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface GroqApi {

    @POST("openai/v1/chat/completions")
    suspend fun chatCompletion(
        @Body request: GroqRequestDto
    ): GroqResponseDto
}
