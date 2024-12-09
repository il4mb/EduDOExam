    package com.capstone.edudoexam.ui.exam

    import android.annotation.SuppressLint
    import android.content.Context
    import android.graphics.Bitmap
    import android.graphics.BitmapFactory
    import android.graphics.ImageFormat
    import android.graphics.Rect
    import android.graphics.YuvImage
    import android.util.Log
    import androidx.camera.core.ImageAnalysis
    import androidx.camera.core.ImageProxy
    import com.capstone.edudoexam.components.ImageClassifierHelper
    import com.google.mlkit.vision.common.InputImage
    import com.google.mlkit.vision.face.Face
    import com.google.mlkit.vision.face.FaceDetection
    import com.google.mlkit.vision.face.FaceDetectorOptions
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import java.io.ByteArrayOutputStream

    class FaceAnalyzer(
        context: Context,
        private val callback: FaceDetectionCallback
    ) : ImageAnalysis.Analyzer {

        private val imageClassifier = ImageClassifierHelper(context)

        private val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
        )

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(image: ImageProxy) {

            val mediaImage = image.image ?: return
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    processFaces(faces, image)
                }
                .addOnFailureListener { e ->
                    callback.onNoFace()
                    Log.e("EmotionalAnalyzer", "Face detection failed: ${e.message}")
                }
                .addOnCompleteListener {
                    image.close()
                }
        }

        private fun processFaces(faces: List<Face>, imageProxy: ImageProxy) {
            val bitmap = imageProxy.toBitmap()
            CoroutineScope(Dispatchers.Main).launch {
                val largestFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                largestFace?.let { face ->
                    val expandedBoundingBox = expandBoundingBox(face.boundingBox, -0.2f, imageProxy.width, imageProxy.height)
                    cropFaceFromBitmap(bitmap, expandedBoundingBox, 300, 300)?.let {
                        imageClassifier.classify(it) { label, score ->
                            callback.onFace(it, label, score)
                        }
                    }
                } ?: callback.onNoFace()
            }
        }

        private fun ImageProxy.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer // Y
            val uBuffer = planes[1].buffer // U
            val vBuffer = planes[2].buffer // V

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)


            yBuffer.get(nv21, 0, ySize)

            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            return rotateBitmap(bitmap, imageInfo.rotationDegrees)
        }

        private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        private fun cropFaceFromBitmap(image: Bitmap, boundingBox: Rect, width: Int, height: Int): Bitmap? {
            // Ensure the bounding box is within the image bounds
            val left = maxOf(boundingBox.left, 0)
            val top = maxOf(boundingBox.top, 0)
            val right = minOf(boundingBox.right, image.width)
            val bottom = minOf(boundingBox.bottom, image.height)

            // Check for invalid bounding box dimensions, return null instead of throwing an exception
            if (right <= left || bottom <= top) {
                Log.e("FaceAnalyzer", "Invalid bounding box: $boundingBox")
                return null // Return null if bounding box is invalid
            }

            // Crop the bitmap using the adjusted bounding box
            val croppedBitmap = Bitmap.createBitmap(image, left, top, right - left, bottom - top)

            // Resize the cropped face image to the required size (e.g., 300x300)
            return Bitmap.createScaledBitmap(croppedBitmap, width, height, true)
        }

        private fun expandBoundingBox(boundingBox: Rect, factor: Float, imageWidth: Int, imageHeight: Int): Rect {
            val widthIncrease = (boundingBox.width() * factor).toInt()
            val heightIncrease = (boundingBox.height() * factor).toInt()

            val left = maxOf(boundingBox.left - widthIncrease / 2, 0)
            val top = maxOf(boundingBox.top - heightIncrease / 2, 0)
            val right = minOf(boundingBox.right + widthIncrease / 2, imageWidth)
            val bottom = minOf(boundingBox.bottom + heightIncrease / 2, imageHeight)

            val width = right - left
            val height = bottom - top


            if (width > height) {
                val delta = (width - height) / 2
                return Rect(left, top - delta, right, bottom + delta)
            } else {
                val delta = (height - width) / 2
                return Rect(left - delta, top, right + delta, bottom)
            }
        }

        interface FaceDetectionCallback {
            fun onFace(bitmap: Bitmap, label: String, confidence: Double)
            fun onNoFace()
        }
    }
