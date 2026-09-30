package com.example.util

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.ocr.PrizeBondOcrEngine
import com.example.ocr.ScannedBondCandidate
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraHelper manages CameraX lifecycle, preview binding, and real-time
 * frame analysis with Google ML Kit Text Recognition for Bangladesh Prize Bonds.
 */
class CameraHelper(
  private val onBondsDetected: (List<ScannedBondCandidate>, String) -> Unit,
  private val onError: (Throwable) -> Unit = {}
) {

  companion object {
    private const val TAG = "CameraHelper"
    private const val FRAME_THROTTLE_MS = 250L // 250ms interval for smooth UI and battery efficiency
  }

  private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
  private var cameraProvider: ProcessCameraProvider? = null
  private var camera: Camera? = null
  private var preview: Preview? = null
  private var imageAnalysis: ImageAnalysis? = null

  private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
  private val isProcessingFrame = AtomicBoolean(false)
  private var lastAnalysisTimestamp = 0L

  var isTorchOn: Boolean = false
    private set

  private var appContext: Context? = null

  /**
   * Initializes and starts CameraX preview and real-time ML Kit analysis.
   */
  fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView
  ) {
    appContext = context.applicationContext
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
      try {
        cameraProvider = cameraProviderFuture.get()
        bindCameraUseCases(lifecycleOwner, previewView)
      } catch (exc: Exception) {
        Log.e(TAG, "ProcessCameraProvider initialization failed", exc)
        onError(exc)
      }
    }, ContextCompat.getMainExecutor(context))
  }

  /**
   * Binds preview and analysis use cases to the provided lifecycle owner.
   */
  private fun bindCameraUseCases(
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView
  ) {
    val provider = cameraProvider ?: return

    val cameraSelector = CameraSelector.Builder()
      .requireLensFacing(lensFacing)
      .build()

    // 1. Preview use case
    preview = Preview.Builder()
      .build()
      .also {
        it.surfaceProvider = previewView.surfaceProvider
      }

    // 2. Real-time Image Analysis use case
    imageAnalysis = ImageAnalysis.Builder()
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
      .build()
      .also { analysis ->
        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
          processImageProxy(imageProxy)
        }
      }

    try {
      provider.unbindAll()
      camera = provider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageAnalysis
      )
      isTorchOn = false
    } catch (exc: Exception) {
      Log.e(TAG, "Use case binding failed", exc)
      onError(exc)
    }
  }

  /**
   * Processes each camera frame using ML Kit Text Recognition.
   */
  @OptIn(ExperimentalGetImage::class)
  private fun processImageProxy(imageProxy: ImageProxy) {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastAnalysisTimestamp < FRAME_THROTTLE_MS) {
      imageProxy.close()
      return
    }

    if (!isProcessingFrame.compareAndSet(false, true)) {
      imageProxy.close()
      return
    }

    val mediaImage = imageProxy.image
    if (mediaImage == null) {
      imageProxy.close()
      isProcessingFrame.set(false)
      return
    }

    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

    recognizer.process(inputImage)
      .addOnSuccessListener { visionText ->
        lastAnalysisTimestamp = System.currentTimeMillis()
        val detectedText = visionText.text
        if (detectedText.isNotBlank()) {
          val candidates = PrizeBondOcrEngine.extractBondsFromRecognizedText(detectedText)
          if (candidates.isNotEmpty()) {
            onBondsDetected(candidates, detectedText)
          }
        }
      }
      .addOnFailureListener { exc ->
        Log.w(TAG, "ML Kit text recognition error", exc)
      }
      .addOnCompleteListener {
        imageProxy.close()
        isProcessingFrame.set(false)
      }
  }

  /**
   * Toggles the device flashlight / torch.
   */
  fun toggleTorch(enable: Boolean? = null) {
    val targetState = enable ?: !isTorchOn
    val context = appContext
    val executor = if (context != null) ContextCompat.getMainExecutor(context) else return
    camera?.cameraControl?.enableTorch(targetState)?.addListener({
      isTorchOn = targetState
    }, executor)
  }

  /**
   * Switches between Back and Front cameras.
   */
  fun switchCamera(
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView
  ) {
    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
      CameraSelector.LENS_FACING_FRONT
    } else {
      CameraSelector.LENS_FACING_BACK
    }
    bindCameraUseCases(lifecycleOwner, previewView)
  }

  /**
   * Stops camera processing and cleans up resources.
   */
  fun stopCamera() {
    try {
      cameraProvider?.unbindAll()
    } catch (e: Exception) {
      Log.w(TAG, "Error unbinding camera provider", e)
    }
  }

  /**
   * Shuts down executors and ML Kit recognizer.
   */
  fun release() {
    stopCamera()
    try {
      recognizer.close()
    } catch (e: Exception) {
      Log.w(TAG, "Error closing recognizer", e)
    }
    cameraExecutor.shutdown()
  }
}
