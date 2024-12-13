package com.il4mb.edudoexam.components

import android.content.Context
import android.graphics.Bitmap
import com.il4mb.edudoexam.ml.ModelFinal1
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.exp

class ImageClassifierHelper(val context: Context) {

    private var model: ModelFinal1? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        model = ModelFinal1.newInstance(context)
    }

    fun classify(image: Bitmap, callback: (String, Double) -> Unit) {
        try {
            val resizedBitmap = resizeImage(image)
            val inputBuffer = prepareInputBuffer(resizedBitmap)
            runInference(inputBuffer, callback)
        } catch (e: Exception) {
            callback.invoke("Error: ${e.message}", 0.0)
        }
    }

    private fun resizeImage(image: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(image, 128, 128, false)
    }

    private fun prepareInputBuffer(image: Bitmap): TensorBuffer {
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)
        val floatArray = FloatArray(128 * 128 * 3)
        var index = 0
        for (y in 0 until 128) {
            for (x in 0 until 128) {
                val pixel = image.getPixel(x, y)
                floatArray[index++] = ((pixel shr 16 and 0xFF) / 255.0f)
                floatArray[index++] = ((pixel shr 8 and 0xFF) / 255.0f)
                floatArray[index++] = ((pixel and 0xFF) / 255.0f)
            }
        }
        inputBuffer.loadArray(floatArray)
        return inputBuffer
    }

    private fun runInference(inputBuffer: TensorBuffer, callback: (String, Double) -> Unit) {

        model?.let { model ->

            val outputs = model.process(inputBuffer)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val probabilityArray = outputFeature0.floatArray

            val sumExp = probabilityArray.sumOf { exp(it.toDouble()) }
            val normalizedProbabilities = probabilityArray.map { exp(it.toDouble()) / sumExp }

            val emotions = listOf("Anger", "Fear", "Surprised", "Happy", "Sad", "Disgust", "Neutral")
            val emotionMap = emotions.zip(normalizedProbabilities).toMap()

            val dominantEmotion = emotionMap.maxByOrNull { it.value }

            dominantEmotion?.let {
                callback.invoke(it.key, it.value)
            } ?: callback.invoke("No Face Detected", 0.0)
        }
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onSuccess(label: String, score: Float)
    }
}
