package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class GeminiAiProvider {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    suspend fun generateText(
        model: String,
        apiKey: String,
        prompt: String,
        temperature: Float = 0.7f
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured in Settings."))
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(
                    ContentDto(
                        parts = listOf(PartDto(text = prompt))
                    )
                ),
                generationConfig = GenerationConfigDto(temperature = temperature)
            )

            val cleanModel = model.trim().ifBlank { "gemini-2.5-flash" }
            val response = api.generateContent(cleanModel, apiKey.trim(), request)

            if (response.error != null) {
                val errorMsg = response.error.message ?: "Gemini API error ${response.error.code}"
                return@withContext Result.failure(Exception(errorMsg))
            }

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("No text generated in response."))
            }
        } catch (e: Exception) {
            val safeMessage = when {
                e.message?.contains("Unable to resolve host") == true -> "Network unreachable. Check your internet connection."
                e.message?.contains("timeout") == true -> "Request timed out. Please try again."
                e.message?.contains("400") == true -> "Invalid request or unsupported model parameter."
                e.message?.contains("403") == true -> "API key is invalid or quota exceeded."
                else -> e.localizedMessage ?: "Failed to generate content."
            }
            Result.failure(Exception(safeMessage))
        }
    }
}
