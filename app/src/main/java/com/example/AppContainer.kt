package com.example

import android.content.Context
import com.example.data.ai.AiRepository
import com.example.data.ai.AiRepositoryImpl
import com.example.data.local.NoteRepository
import com.example.data.local.SettingsRepository
import com.example.data.remote.GeminiAiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(val context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepository(context, applicationScope)
    }

    val geminiAiProvider: GeminiAiProvider by lazy {
        GeminiAiProvider()
    }

    val aiRepository: AiRepository by lazy {
        AiRepositoryImpl(geminiAiProvider, settingsRepository)
    }
}
