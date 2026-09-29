package com.example.data.ai

import com.example.data.local.SettingsRepository
import com.example.data.model.Note
import com.example.data.remote.GeminiAiProvider
import kotlinx.coroutines.flow.first

interface AiRepository {
    suspend fun executeNaturalLanguageInstruction(instruction: String, text: String): Result<String>
    suspend fun summarize(text: String): Result<String>
    suspend fun rewrite(text: String, style: String = "concise and clear"): Result<String>
    suspend fun fixGrammar(text: String): Result<String>
    suspend fun translate(text: String, targetLanguage: String): Result<String>
    suspend fun autoTag(title: String, content: String): Result<List<String>>
    suspend fun autoCategorize(title: String, content: String, existingCategories: List<String>): Result<String>
    suspend fun reRankCandidates(query: String, candidates: List<Note>): Result<List<String>>
}

class AiRepositoryImpl(
    private val provider: GeminiAiProvider,
    private val settingsRepository: SettingsRepository
) : AiRepository {

    private suspend fun getCredentials(): Pair<String, String>? {
        val settings = settingsRepository.settingsFlow.first()
        if (!settings.geminiEnabled || settings.geminiApiKey.isBlank()) {
            return null
        }
        return Pair(settings.geminiModel, settings.geminiApiKey)
    }

    override suspend fun executeNaturalLanguageInstruction(instruction: String, text: String): Result<String> {
        val creds = getCredentials()
            ?: return Result.failure(IllegalStateException("Gemini AI is disabled or API key is missing. Please enable Gemini and enter your API key in Settings."))

        val prompt = """
            You are an expert AI writing assistant integrated into a mobile notes application.
            The user has provided the following text:

            === INPUT TEXT ===
            $text
            === END INPUT TEXT ===

            User's natural-language instruction:
            "$instruction"

            Instructions for your response:
            1. Perform the requested operation accurately according to the user's instruction.
            2. Return ONLY the transformed or generated text.
            3. Do NOT include conversational filler, meta-talk, greetings, or prefixes like "Here is the rewritten text:" unless the user's instruction explicitly asks for an explanation.
            4. Preserve the language requested by the user. If translation is requested, translate accurately.
        """.trimIndent()

        return provider.generateText(creds.first, creds.second, prompt)
    }

    override suspend fun summarize(text: String): Result<String> {
        val creds = getCredentials()
            ?: return Result.failure(IllegalStateException("Gemini AI is disabled or API key is missing in Settings."))
        val prompt = "Provide a clean, bulleted or short executive summary of the following note content:\n\n$text"
        return provider.generateText(creds.first, creds.second, prompt)
    }

    override suspend fun rewrite(text: String, style: String): Result<String> {
        val creds = getCredentials()
            ?: return Result.failure(IllegalStateException("Gemini AI is disabled or API key is missing in Settings."))
        val prompt = "Rewrite the following note text to be $style while preserving its core meaning:\n\n$text"
        return provider.generateText(creds.first, creds.second, prompt)
    }

    override suspend fun fixGrammar(text: String): Result<String> {
        val creds = getCredentials()
            ?: return Result.failure(IllegalStateException("Gemini AI is disabled or API key is missing in Settings."))
        val prompt = "Proofread and fix any grammatical, punctuation, or spelling errors in the following text. Return only the corrected text:\n\n$text"
        return provider.generateText(creds.first, creds.second, prompt)
    }

    override suspend fun translate(text: String, targetLanguage: String): Result<String> {
        val creds = getCredentials()
            ?: return Result.failure(IllegalStateException("Gemini AI is disabled or API key is missing in Settings."))
        val prompt = "Translate the following text accurately into $targetLanguage. Return only the translated text:\n\n$text"
        return provider.generateText(creds.first, creds.second, prompt)
    }

    override suspend fun autoTag(title: String, content: String): Result<List<String>> {
        val creds = getCredentials()
            ?: return Result.failure(IllegalStateException("Gemini AI is disabled or API key is missing in Settings."))
        val prompt = """
            Based on the following note title and content, generate 3 to 5 relevant short hashtags (comma-separated, without '#' prefix, e.g. productivity, work, project).
            Title: $title
            Content: ${content.take(500)}
            Output only the comma-separated words.
        """.trimIndent()

        val result = provider.generateText(creds.first, creds.second, prompt)
        return result.map { raw ->
            raw.split(",")
                .map { it.trim().removePrefix("#").lowercase() }
                .filter { it.isNotBlank() && it.length < 25 }
        }
    }

    override suspend fun autoCategorize(title: String, content: String, existingCategories: List<String>): Result<String> {
        val creds = getCredentials()
            ?: return Result.failure(IllegalStateException("Gemini AI is disabled or API key is missing in Settings."))
        val catListStr = existingCategories.joinToString(", ")
        val prompt = """
            Given the note title: "$title"
            And content snippet: "${content.take(300)}"
            Choose the single best matching category from this existing list: [$catListStr].
            If none fit well, output "General".
            Respond with ONLY the exact category name.
        """.trimIndent()

        val result = provider.generateText(creds.first, creds.second, prompt)
        return result.map { it.lines().firstOrNull()?.trim() ?: "General" }
    }

    override suspend fun reRankCandidates(query: String, candidates: List<Note>): Result<List<String>> {
        val creds = getCredentials()
            ?: return Result.failure(IllegalStateException("Gemini AI is disabled or API key is missing in Settings."))

        if (candidates.isEmpty()) return Result.success(emptyList())
        if (candidates.size == 1) return Result.success(listOf(candidates.first().id))

        // Build concise candidate representations with index
        val candidatePreviews = candidates.take(15).mapIndexed { idx, note ->
            val preview = note.blocks.joinToString(" ") { it.content }.take(100)
            "[$idx] ID:${note.id} Title:${note.title} Content:$preview"
        }.joinToString("\n")

        val prompt = """
            User search query: "$query"
            Below are candidate notes:
            $candidatePreviews
            Rank the candidate note IDs from most relevant to least relevant to the user query.
            Return ONLY a comma-separated list of IDs in order of relevance (e.g. id1, id2, id3).
        """.trimIndent()

        val result = provider.generateText(creds.first, creds.second, prompt, temperature = 0.2f)
        return result.map { raw ->
            val idList = raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val existingIds = candidates.map { it.id }.toSet()
            val validRanked = idList.filter { it in existingIds }
            val unranked = candidates.map { it.id }.filterNot { it in validRanked }
            validRanked + unranked
        }
    }
}
