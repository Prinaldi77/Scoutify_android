package com.pab.scoutify.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaceDetectionHelper @Inject constructor() {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun detectFaces(imageProxy: ImageProxy): List<Face> {
        return try {
            val mediaImage = imageProxy.image ?: return emptyList()
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image).await()
        } catch (e: Exception) {
            emptyList()
        } finally {
            imageProxy.close()
        }
    }

    fun isFaceInOval(face: Face, width: Int, height: Int): Boolean {
        val boundingBox = face.boundingBox
        
        // Oval area parameters (assuming oval is centered and takes ~70% of width/height)
        val ovalLeft = width * 0.15f
        val ovalRight = width * 0.85f
        val ovalTop = height * 0.15f
        val ovalBottom = height * 0.85f
        
        // Check if face bounding box is mostly within the oval area
        val isCentered = boundingBox.centerX() > ovalLeft && boundingBox.centerX() < ovalRight &&
                         boundingBox.centerY() > ovalTop && boundingBox.centerY() < ovalBottom
        
        val isLargeEnough = boundingBox.width() > width * 0.3f
        
        return isCentered && isLargeEnough
    }
}
