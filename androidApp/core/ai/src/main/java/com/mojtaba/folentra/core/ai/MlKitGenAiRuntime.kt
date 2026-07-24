package com.mojtaba.folentra.core.ai

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.flow.collect

/** Small boundary around the beta ML Kit API so provider logic remains unit-testable. */
interface OnDeviceGenAiRuntime {
    suspend fun status(): OnDeviceModelStatus
    suspend fun prepare(): OnDeviceModelStatus
    suspend fun generate(prompt: String): String
}

enum class OnDeviceModelStatus {
    Available,
    Downloadable,
    Downloading,
    Unavailable,
}

class MlKitGenAiRuntime : OnDeviceGenAiRuntime {
    private val model by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Generation.getClient() }

    override suspend fun status(): OnDeviceModelStatus = model.checkStatus().toModelStatus()

    override suspend fun prepare(): OnDeviceModelStatus {
        return when (val current = status()) {
            OnDeviceModelStatus.Downloadable -> {
                var completed = false
                model.download().collect { downloadStatus ->
                    when (downloadStatus) {
                        DownloadStatus.DownloadCompleted -> completed = true
                        is DownloadStatus.DownloadFailed -> throw downloadStatus.e
                        else -> Unit
                    }
                }
                if (completed) status() else OnDeviceModelStatus.Downloading
            }
            else -> current
        }
    }

    override suspend fun generate(prompt: String): String {
        val response = model.generateContent(prompt)
        return response.candidates.firstOrNull()?.text.orEmpty().trim()
    }

    private fun Int.toModelStatus(): OnDeviceModelStatus = when (this) {
        FeatureStatus.AVAILABLE -> OnDeviceModelStatus.Available
        FeatureStatus.DOWNLOADABLE -> OnDeviceModelStatus.Downloadable
        FeatureStatus.DOWNLOADING -> OnDeviceModelStatus.Downloading
        else -> OnDeviceModelStatus.Unavailable
    }
}
