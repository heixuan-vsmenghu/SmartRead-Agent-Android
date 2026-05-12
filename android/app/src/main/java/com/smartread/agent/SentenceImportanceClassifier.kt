package com.smartread.agent

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

data class SentenceImportanceResult(
    val score: Float,
    val level: String,
    val source: String,
)

class SentenceImportanceClassifier(context: Context) : Closeable {
    private val interpreter: Interpreter? = runCatching {
        Interpreter(loadModelBuffer(context))
    }.getOrNull()

    val isModelLoaded: Boolean
        get() = interpreter != null

    fun classify(features: FloatArray): SentenceImportanceResult {
        val normalized = normalizeFeatures(features)
        val modelScore = interpreter?.let { model ->
            runCatching {
                val input = arrayOf(normalized)
                val output = Array(1) { FloatArray(1) }
                model.run(input, output)
                output[0][0].coerceIn(0f, 1f)
            }.getOrNull()
        }
        val score = modelScore ?: fallbackScore(normalized)

        return SentenceImportanceResult(
            score = score,
            level = when {
                score >= 0.70f -> "高"
                score >= 0.40f -> "中"
                else -> "低"
            },
            source = if (modelScore != null) "LiteRT 本地模型" else "规则评分兜底",
        )
    }

    override fun close() {
        interpreter?.close()
    }

    private fun loadModelBuffer(context: Context): ByteBuffer {
        val bytes = context.assets.open(MODEL_FILE_NAME).use { it.readBytes() }
        return ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .apply {
                put(bytes)
                rewind()
            }
    }

    private fun normalizeFeatures(features: FloatArray): FloatArray {
        val normalized = FloatArray(INPUT_SIZE)
        for (index in normalized.indices) {
            normalized[index] = features.getOrElse(index) { 0f }.coerceIn(0f, 1f)
        }
        return normalized
    }

    private fun fallbackScore(features: FloatArray): Float {
        val score = (
            0.22f * features[0] +
                0.31f * features[1] +
                0.20f * features[2] +
                0.12f * features[3] +
                0.25f * features[4] -
                0.05f
            )
        return max(0.05f, score).coerceAtMost(0.98f)
    }

    companion object {
        private const val MODEL_FILE_NAME = "sentence_importance_model.tflite"
        private const val INPUT_SIZE = 5
    }
}
