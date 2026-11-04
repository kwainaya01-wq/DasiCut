package com.example.media

import androidx.compose.ui.graphics.ColorMatrix
import com.example.model.VideoFilter

object ColorFilterMatrixHelper {

  private val identityArray = floatArrayOf(
    1f, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f
  )

  fun getMatrix(filter: VideoFilter, intensity: Float): ColorMatrix {
    val clampedIntensity = intensity.coerceIn(0f, 1f)
    if (filter == VideoFilter.ORIGINAL || clampedIntensity <= 0.01f) {
      return ColorMatrix(identityArray)
    }

    val targetArray = when (filter) {
      VideoFilter.ORIGINAL -> identityArray

      VideoFilter.WARM_DESI -> floatArrayOf(
        1.25f, 0.05f, 0.0f, 0f, 15f,
        0.05f, 1.15f, 0.0f, 0f, 10f,
        0.0f, 0.0f, 0.85f, 0f, -10f,
        0f, 0f, 0f, 1f, 0f
      )

      VideoFilter.BOLLYWOOD -> floatArrayOf(
        1.35f, 0.0f, 0.0f, 0f, 20f,
        0.0f, 1.05f, 0.0f, 0f, 5f,
        -0.1f, 0.0f, 1.25f, 0f, 10f,
        0f, 0f, 0f, 1f, 0f
      )

      VideoFilter.COOL_TEAL -> floatArrayOf(
        0.85f, 0.0f, 0.0f, 0f, -10f,
        0.0f, 1.10f, 0.0f, 0f, 8f,
        0.0f, 0.1f, 1.30f, 0f, 18f,
        0f, 0f, 0f, 1f, 0f
      )

      VideoFilter.VINTAGE_90S -> floatArrayOf(
        0.90f, 0.20f, 0.10f, 0f, 20f,
        0.15f, 0.85f, 0.10f, 0f, 15f,
        0.10f, 0.15f, 0.70f, 0f, 5f,
        0f, 0f, 0f, 1f, 0f
      )

      VideoFilter.MOODY_NOIR -> floatArrayOf(
        0.45f, 0.45f, 0.45f, 0f, -10f,
        0.45f, 0.45f, 0.45f, 0f, -10f,
        0.45f, 0.45f, 0.45f, 0f, -10f,
        0f, 0f, 0f, 1f, 0f
      )

      VideoFilter.EMERALD_DREAM -> floatArrayOf(
        0.80f, 0.0f, 0.0f, 0f, -5f,
        0.05f, 1.35f, 0.05f, 0f, 25f,
        0.0f, 0.10f, 0.95f, 0f, 5f,
        0f, 0f, 0f, 1f, 0f
      )

      VideoFilter.CYBER_NEON -> floatArrayOf(
        1.30f, 0.0f, 0.20f, 0f, 15f,
        0.0f, 1.10f, 0.0f, 0f, -5f,
        0.20f, 0.0f, 1.40f, 0f, 20f,
        0f, 0f, 0f, 1f, 0f
      )

      VideoFilter.SUNSET_CHAI -> floatArrayOf(
        1.35f, 0.10f, 0.0f, 0f, 25f,
        0.10f, 1.05f, 0.0f, 0f, 5f,
        -0.05f, 0.0f, 0.75f, 0f, -20f,
        0f, 0f, 0f, 1f, 0f
      )

      VideoFilter.MONOCHROME -> floatArrayOf(
        0.33f, 0.59f, 0.11f, 0f, 0f,
        0.33f, 0.59f, 0.11f, 0f, 0f,
        0.33f, 0.59f, 0.11f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
      )
    }

    // Linearly interpolate between identity and targetArray based on clampedIntensity
    val blended = FloatArray(20)
    for (i in 0 until 20) {
      blended[i] = identityArray[i] + (targetArray[i] - identityArray[i]) * clampedIntensity
    }

    return ColorMatrix(blended)
  }

  fun getAndroidGraphicsColorFilter(filter: VideoFilter, intensity: Float): android.graphics.ColorMatrixColorFilter {
    val cm = getMatrix(filter, intensity)
    val androidCm = android.graphics.ColorMatrix(cm.values)
    return android.graphics.ColorMatrixColorFilter(androidCm)
  }
}
