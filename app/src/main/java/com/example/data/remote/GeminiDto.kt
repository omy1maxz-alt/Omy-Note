package com.example.data.remote

data class GenerateContentRequest(
    val contents: List<ContentDto>,
    val generationConfig: GenerationConfigDto? = null
)

data class ContentDto(
    val parts: List<PartDto>,
    val role: String? = null
)

data class PartDto(
    val text: String? = null
)

data class GenerationConfigDto(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null
)

data class GenerateContentResponse(
    val candidates: List<CandidateDto>? = null,
    val error: GeminiErrorDto? = null
)

data class CandidateDto(
    val content: ContentDto? = null,
    val finishReason: String? = null
)

data class GeminiErrorDto(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
