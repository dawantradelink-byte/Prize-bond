package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ScannedBondCandidate(
  val id: String = java.util.UUID.randomUUID().toString(),
  val number: String,
  val series: String = "All",
  val rawSnippet: String = "",
  var isSelected: Boolean = true
)

object PrizeBondOcrEngine {

  private val BENGALI_SERIES_LIST = listOf(
    "কখ", "খগ", "গঘ", "ঘঙ", "ঙচ", "চছ", "ছজ", "জঝ", "ঝঞ", "টঠ",
    "ঠড", "ঢণ", "তথ", "দধ", "নপ", "ফব", "ভম", "যর", "লশ", "ষস", "হড়", "ঢ়য়"
  )

  /**
   * Converts Bengali digits (০-৯) to Latin digits (0-9).
   */
  fun convertBengaliDigitsToLatin(input: String): String {
    val bengaliToLatin = mapOf(
      '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
      '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )
    val sb = java.lang.StringBuilder()
    for (ch in input) {
      sb.append(bengaliToLatin[ch] ?: ch)
    }
    return sb.toString()
  }

  /**
   * Processes a Bitmap through Google ML Kit Text Recognition
   * and extracts valid 7-digit 100 Taka prize bond numbers and series.
   */
  suspend fun scanBitmap(bitmap: Bitmap): List<ScannedBondCandidate> = withContext(Dispatchers.Default) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val inputImage = InputImage.fromBitmap(bitmap, 0)

    try {
      val visionText = suspendCancellableCoroutine { continuation ->
        recognizer.process(inputImage)
          .addOnSuccessListener { text ->
            continuation.resume(text)
          }
          .addOnFailureListener { e ->
            continuation.resumeWithException(e)
          }
      }

      extractBondsFromRecognizedText(visionText.text)
    } finally {
      recognizer.close()
    }
  }

  /**
   * Processes an image Uri through Google ML Kit Text Recognition.
   */
  suspend fun scanUri(context: Context, uri: Uri): List<ScannedBondCandidate> = withContext(Dispatchers.IO) {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    if (bitmap == null) {
      return@withContext emptyList()
    }
    scanBitmap(bitmap)
  }

  /**
   * Parses recognized text lines and blocks to isolate 7-digit bond numbers
   * and find associated series identifiers.
   */
  fun extractBondsFromRecognizedText(rawText: String): List<ScannedBondCandidate> {
    val normalizedText = convertBengaliDigitsToLatin(rawText)
    val candidates = mutableListOf<ScannedBondCandidate>()
    val seenNumbers = mutableSetOf<String>()

    val lines = normalizedText.lines()
    var activeSeries = "All"

    for (line in lines) {
      val trimmedLine = line.trim()
      if (trimmedLine.isBlank()) continue

      // Look for series in this line (Bengali or English letters)
      val lineSeries = findSeriesInSnippet(line)
      if (lineSeries != null) {
        activeSeries = lineSeries
      }
      val detectedSeries = lineSeries ?: activeSeries

      // Regex matches 7 continuous digits, or groups of 7 digits
      val digitMatcher = Regex("(?<!\\d)\\d{7}(?!\\d)").findAll(trimmedLine)

      for (match in digitMatcher) {
        val number = match.value
        if (number !in seenNumbers) {
          seenNumbers.add(number)
          candidates.add(
            ScannedBondCandidate(
              number = number,
              series = detectedSeries,
              rawSnippet = trimmedLine,
              isSelected = true
            )
          )
        }
      }

      // If no strict 7-digit match found, look for 6-8 digit tokens with spaces (e.g. "012 3456")
      if (digitMatcher.none()) {
        val spaceCleaned = trimmedLine.replace(" ", "")
        val spaceMatcher = Regex("(?<!\\d)\\d{7}(?!\\d)").findAll(spaceCleaned)
        for (match in spaceMatcher) {
          val number = match.value
          if (number !in seenNumbers) {
            seenNumbers.add(number)
            candidates.add(
              ScannedBondCandidate(
                number = number,
                series = detectedSeries,
                rawSnippet = trimmedLine,
                isSelected = true
              )
            )
          }
        }
      }
    }

    return candidates
  }

  private fun findSeriesInSnippet(snippet: String): String? {
    for (series in BENGALI_SERIES_LIST) {
      if (snippet.contains(series)) {
        return series
      }
    }
    return null
  }
}
