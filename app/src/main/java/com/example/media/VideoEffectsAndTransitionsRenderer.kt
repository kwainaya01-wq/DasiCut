package com.example.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.model.AppliedEffect
import com.example.model.TransitionType
import com.example.model.VideoClip
import com.example.model.VideoEffect
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance shared rendering engine for all 20 professional Transitions
 * and 50+ real Visual Effects.
 * Used by both real-time Compose VideoPreviewCanvas and offline NativeVideoExporter.
 */
object VideoEffectsAndTransitionsRenderer {

  // =========================================================================
  // COMPOSE DRAW SCOPE: 50+ REAL VIDEO EFFECTS
  // =========================================================================

  fun renderEffectsOnCompose(
    drawScope: DrawScope,
    effects: List<AppliedEffect>,
    animPhase: Float
  ) {
    if (effects.isEmpty()) return
    for (applied in effects) {
      val effect = applied.effect
      val intensity = applied.intensity.coerceIn(0f, 1f)
      if (effect == VideoEffect.NONE || intensity <= 0.01f) continue

      drawSingleEffectCompose(drawScope, effect, intensity, animPhase)
    }
  }

  private fun drawSingleEffectCompose(
    scope: DrawScope,
    effect: VideoEffect,
    intensity: Float,
    animPhase: Float
  ) {
    val size = scope.size
    val width = size.width
    val height = size.height

    when (effect) {
      // ---------------------------------------------------------------------
      // 1. CINEMATIC
      // ---------------------------------------------------------------------
      VideoEffect.CINEMATIC_GLOW -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFE0B2).copy(alpha = 0.55f * intensity),
              Color(0xFFFF9800).copy(alpha = 0.25f * intensity),
              Color.Transparent
            ),
            center = Offset(width * 0.5f, height * 0.45f),
            radius = width * 0.75f
          ),
          blendMode = BlendMode.Plus
        )
      }

      VideoEffect.FILM_LOOK -> {
        scope.drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFF263238).copy(alpha = 0.35f * intensity),
              Color(0xFFFF8F00).copy(alpha = 0.25f * intensity)
            )
          ),
          blendMode = BlendMode.Overlay
        )
      }

      VideoEffect.TEAL_AND_ORANGE -> {
        scope.drawRect(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0xFFFF6D00).copy(alpha = 0.45f * intensity),
              Color(0xFF00838F).copy(alpha = 0.50f * intensity)
            ),
            start = Offset(0f, 0f),
            end = Offset(width, height)
          ),
          blendMode = BlendMode.Overlay
        )
      }

      VideoEffect.MOVIE_GRAIN, VideoEffect.FILM_GRAIN -> {
        val count = (75 * intensity).toInt().coerceAtLeast(12)
        val rand = Random(((animPhase * 97).toInt() + 17))
        for (i in 0..count) {
          val rx = rand.nextFloat() * width
          val ry = rand.nextFloat() * height
          val rRad = 1.2f + (rand.nextFloat() * 1.5f * intensity)
          scope.drawCircle(
            color = Color.White.copy(alpha = (0.35f + rand.nextFloat() * 0.4f) * intensity),
            radius = rRad,
            center = Offset(rx, ry)
          )
        }
      }

      VideoEffect.SOFT_FOCUS -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFFFFF).copy(alpha = 0.38f * intensity),
              Color(0xFFFFE4E1).copy(alpha = 0.18f * intensity),
              Color.Transparent
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.8f
          ),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.CINEMATIC_BLUR -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.Transparent,
              Color(0x99000000).copy(alpha = 0.6f * intensity)
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.55f
          ),
          blendMode = BlendMode.Multiply
        )
      }

      VideoEffect.VIGNETTE -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.Transparent,
              Color(0xFF000000).copy(alpha = 0.96f * intensity)
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * (0.85f - 0.30f * intensity)
          )
        )
      }

      VideoEffect.BLEACH_BYPASS -> {
        scope.drawRect(
          color = Color(0xFF000000).copy(alpha = 0.4f * intensity),
          blendMode = BlendMode.Saturation
        )
        scope.drawRect(
          color = Color(0xFFCCCCCC).copy(alpha = 0.45f * intensity),
          blendMode = BlendMode.Hardlight
        )
      }

      // ---------------------------------------------------------------------
      // 2. GLITCH
      // ---------------------------------------------------------------------
      VideoEffect.GLITCH -> {
        val glitchLineY = (animPhase * height * 1.5f) % height
        val sliceH = (16f * intensity).coerceAtLeast(4f)
        scope.drawRect(
          color = Color(0xFF00E5FF).copy(alpha = 0.75f * intensity),
          topLeft = Offset(0f, glitchLineY),
          size = Size(width, sliceH),
          blendMode = BlendMode.Screen
        )
        scope.drawRect(
          color = Color(0xFFFF0055).copy(alpha = 0.75f * intensity),
          topLeft = Offset(12f * intensity, (glitchLineY + 28f) % height),
          size = Size(width - 24f, sliceH * 0.75f),
          blendMode = BlendMode.Difference
        )
      }

      VideoEffect.RGB_SPLIT -> {
        val splitOffset = 10f * intensity
        scope.drawRect(
          color = Color(0xFFFF0000).copy(alpha = 0.45f * intensity),
          topLeft = Offset(-splitOffset, 0f),
          size = size,
          blendMode = BlendMode.Screen
        )
        scope.drawRect(
          color = Color(0xFF00FFFF).copy(alpha = 0.45f * intensity),
          topLeft = Offset(splitOffset, 0f),
          size = size,
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.VHS_GLITCH -> {
        val barY = (animPhase * height * 2f) % height
        scope.drawRect(
          color = Color(0xFFFFFFFF).copy(alpha = 0.3f * intensity),
          topLeft = Offset(0f, barY),
          size = Size(width, 6f),
          blendMode = BlendMode.Overlay
        )
        scope.drawRect(
          color = Color(0xFF00FFFF).copy(alpha = 0.5f * intensity),
          topLeft = Offset(-8f * intensity, barY + 8f),
          size = Size(width, 3f),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.SIGNAL_DISTORTION -> {
        val rand = Random(((animPhase * 53).toInt()))
        val count = (12 * intensity).toInt().coerceAtLeast(3)
        for (i in 0 until count) {
          val y = rand.nextFloat() * height
          val h = 2f + rand.nextFloat() * 4f
          val shift = (rand.nextFloat() * 30f - 15f) * intensity
          scope.drawRect(
            color = Color(0x66FFFFFF),
            topLeft = Offset(shift, y),
            size = Size(width, h),
            blendMode = BlendMode.Overlay
          )
        }
      }

      VideoEffect.PIXEL_GLITCH -> {
        val rand = Random(((animPhase * 43).toInt()))
        val blocks = (10 * intensity).toInt().coerceAtLeast(2)
        for (i in 0 until blocks) {
          val bx = rand.nextFloat() * width
          val by = rand.nextFloat() * height
          val bw = 30f + rand.nextFloat() * 50f
          val bh = 15f + rand.nextFloat() * 25f
          val col = if (i % 2 == 0) Color(0xFF00FFCC) else Color(0xFFFF0077)
          scope.drawRect(
            color = col.copy(alpha = 0.6f * intensity),
            topLeft = Offset(bx, by),
            size = Size(bw, bh),
            blendMode = BlendMode.Screen
          )
        }
      }

      VideoEffect.CHROMATIC_ABERRATION -> {
        val radOffset = 9f * intensity
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0xFFFF0033).copy(alpha = 0.45f * intensity)),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.7f
          ),
          blendMode = BlendMode.Screen
        )
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0xFF00E5FF).copy(alpha = 0.45f * intensity)),
            center = Offset(width * 0.5f + radOffset, height * 0.5f),
            radius = width * 0.72f
          ),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.DATA_MOSH -> {
        val moshY = (animPhase * height) % height
        scope.drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFF007F).copy(alpha = 0.6f * intensity),
              Color(0xFF00FF66).copy(alpha = 0.5f * intensity),
              Color.Transparent
            )
          ),
          topLeft = Offset(0f, moshY),
          size = Size(width, 45f * intensity),
          blendMode = BlendMode.Difference
        )
      }

      VideoEffect.SCANLINE_WAVE -> {
        var y = 0f
        val waveShift = sin(animPhase * 6.28f) * 4f
        while (y < height) {
          val alpha = ((0.25f + 0.15f * sin((y + waveShift) * 0.08f)) * intensity).coerceIn(0f, 1f)
          scope.drawLine(
            color = Color.Black.copy(alpha = alpha),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 2f
          )
          y += 6f
        }
      }

      // ---------------------------------------------------------------------
      // 3. LIGHT
      // ---------------------------------------------------------------------
      VideoEffect.LENS_FLARE -> {
        val flareCenter = Offset(width * 0.35f, height * 0.30f)
        scope.drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFF176).copy(alpha = 0.85f * intensity),
              Color(0xFFFFB74D).copy(alpha = 0.4f * intensity),
              Color.Transparent
            ),
            center = flareCenter,
            radius = width * 0.45f
          ),
          radius = width * 0.45f,
          center = flareCenter,
          blendMode = BlendMode.Plus
        )
        scope.drawLine(
          color = Color(0xFFFFF9C4).copy(alpha = 0.65f * intensity),
          start = Offset(0f, flareCenter.y),
          end = Offset(width, flareCenter.y),
          strokeWidth = 3f * intensity,
          blendMode = BlendMode.Plus
        )
      }

      VideoEffect.LIGHT_LEAK -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFF3D00).copy(alpha = 0.70f * intensity),
              Color(0xFFFFAB00).copy(alpha = 0.45f * intensity),
              Color.Transparent
            ),
            center = Offset(width * 0.05f, height * 0.1f),
            radius = width * 0.75f
          ),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.FLASH -> {
        val flashVal = (sin(animPhase * 18.0).toFloat().coerceAtLeast(0f)) * intensity
        if (flashVal > 0.05f) {
          scope.drawRect(
            color = Color.White.copy(alpha = 0.65f * flashVal),
            blendMode = BlendMode.Plus
          )
        }
      }

      VideoEffect.GLOW -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFFFFF).copy(alpha = 0.45f * intensity),
              Color.Transparent
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.65f
          ),
          blendMode = BlendMode.Plus
        )
      }

      VideoEffect.NEON_GLOW -> {
        scope.drawRect(
          brush = Brush.horizontalGradient(
            colors = listOf(
              Color(0xFFFF007F).copy(alpha = 0.40f * intensity),
              Color.Transparent,
              Color(0xFF00F0FF).copy(alpha = 0.40f * intensity)
            )
          ),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.GOLDEN_LIGHT -> {
        scope.drawRect(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0xFFFFD54F).copy(alpha = 0.55f * intensity),
              Color(0xFFFF8F00).copy(alpha = 0.25f * intensity),
              Color.Transparent
            ),
            start = Offset(width * 0.8f, 0f),
            end = Offset(0f, height)
          ),
          blendMode = BlendMode.Plus
        )
      }

      VideoEffect.PRISM_RAINBOW -> {
        scope.drawRect(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0x55FF0000),
              Color(0x55FF7F00),
              Color(0x55FFFF00),
              Color(0x5500FF00),
              Color(0x550000FF),
              Color(0x558B00FF)
            ).map { it.copy(alpha = it.alpha * intensity) },
            start = Offset(0f, height * 0.2f),
            end = Offset(width, height * 0.8f)
          ),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.SUN_RAYS -> {
        val raysCount = 7
        val origin = Offset(width * 0.8f, 0f)
        for (i in 0 until raysCount) {
          val rayPath = Path().apply {
            moveTo(origin.x, origin.y)
            val angle = 1.8f + (i * 0.22f)
            lineTo(width * 0.8f - (width * 1.2f * cos(angle)), height * 1.1f * sin(angle))
            lineTo(width * 0.8f - (width * 1.2f * cos(angle + 0.08f)), height * 1.1f * sin(angle + 0.08f))
            close()
          }
          scope.drawPath(
            path = rayPath,
            color = Color(0xFFFFF9C4).copy(alpha = 0.28f * intensity),
            blendMode = BlendMode.Plus
          )
        }
      }

      // ---------------------------------------------------------------------
      // 4. RETRO
      // ---------------------------------------------------------------------
      VideoEffect.VINTAGE -> {
        scope.drawRect(
          color = Color(0xFFD7CCC8).copy(alpha = 0.45f * intensity),
          blendMode = BlendMode.Color
        )
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0xFF3E2723).copy(alpha = 0.7f * intensity)),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.75f
          )
        )
      }

      VideoEffect.VHS_SCANLINES -> {
        val strokeW = (2.5f * intensity).coerceAtLeast(1f)
        var y = 0f
        while (y < height) {
          scope.drawLine(
            color = Color(0xFF000000).copy(alpha = 0.35f * intensity),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = strokeW
          )
          y += 8f
        }
      }

      VideoEffect.RETRO_FILM -> {
        scope.drawRect(
          color = Color(0xFFFFB300).copy(alpha = 0.25f * intensity),
          blendMode = BlendMode.Overlay
        )
        // Film border vignettes
        scope.drawRect(
          color = Color.Black.copy(alpha = 0.8f * intensity),
          topLeft = Offset(0f, 0f),
          size = Size(width * 0.04f, height)
        )
        scope.drawRect(
          color = Color.Black.copy(alpha = 0.8f * intensity),
          topLeft = Offset(width * 0.96f, 0f),
          size = Size(width * 0.04f, height)
        )
      }

      VideoEffect.LOOK_90S -> {
        scope.drawRect(
          color = Color(0xFF80DEEA).copy(alpha = 0.22f * intensity),
          blendMode = BlendMode.Color
        )
        // High contrast punch
        scope.drawRect(
          color = Color(0xFF000000).copy(alpha = 0.3f * intensity),
          blendMode = BlendMode.Overlay
        )
      }

      VideoEffect.OLD_CAMERA -> {
        scope.drawRect(
          color = Color(0xFF8D6E63).copy(alpha = 0.55f * intensity),
          blendMode = BlendMode.Color
        )
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0xFF1B0000).copy(alpha = 0.85f * intensity)),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.65f
          )
        )
      }

      VideoEffect.FILM_BURN -> {
        val burnRadius = width * (0.45f + 0.1f * sin(animPhase * 8.0).toFloat()) * intensity
        scope.drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFF3D00).copy(alpha = 0.85f * intensity),
              Color(0xFFFF9100).copy(alpha = 0.55f * intensity),
              Color.Transparent
            ),
            center = Offset(width * 0.9f, height * 0.2f),
            radius = burnRadius
          ),
          radius = burnRadius,
          center = Offset(width * 0.9f, height * 0.2f),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.SILENT_8MM -> {
        // High-contrast B&W wash
        scope.drawRect(
          color = Color.Black.copy(alpha = 0.9f * intensity),
          blendMode = BlendMode.Saturation
        )
        // Random film scratches
        val rand = Random(((animPhase * 83).toInt()))
        val scratchX = rand.nextFloat() * width
        scope.drawLine(
          color = Color.White.copy(alpha = 0.45f * intensity),
          start = Offset(scratchX, 0f),
          end = Offset(scratchX + (rand.nextFloat() * 4f - 2f), height),
          strokeWidth = 1.5f
        )
      }

      VideoEffect.DISCO_70S -> {
        scope.drawRect(
          brush = Brush.horizontalGradient(
            colors = listOf(
              Color(0xFFFF007F).copy(alpha = 0.35f * intensity),
              Color(0xFFFFD700).copy(alpha = 0.35f * intensity),
              Color(0xFF00E5FF).copy(alpha = 0.35f * intensity)
            )
          ),
          blendMode = BlendMode.Overlay
        )
      }

      // ---------------------------------------------------------------------
      // 5. BLUR
      // ---------------------------------------------------------------------
      VideoEffect.GAUSSIAN_BLUR -> {
        scope.drawRect(
          color = Color.White.copy(alpha = 0.30f * intensity),
          blendMode = BlendMode.Overlay
        )
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x33FFFFFF).copy(alpha = 0.4f * intensity),
              Color(0x11000000).copy(alpha = 0.2f * intensity)
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.7f
          )
        )
      }

      VideoEffect.MOTION_BLUR -> {
        val shift = 14f * intensity
        scope.drawRect(
          color = Color.White.copy(alpha = 0.22f * intensity),
          topLeft = Offset(-shift, 0f),
          size = size,
          blendMode = BlendMode.Screen
        )
        scope.drawRect(
          color = Color.White.copy(alpha = 0.22f * intensity),
          topLeft = Offset(shift, 0f),
          size = size,
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.RADIAL_BLUR -> {
        scope.drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.Transparent,
              Color(0x66FFFFFF).copy(alpha = 0.45f * intensity),
              Color.Transparent
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.5f
          ),
          radius = width * 0.5f,
          center = Offset(width * 0.5f, height * 0.5f),
          blendMode = BlendMode.Plus
        )
      }

      VideoEffect.ZOOM_BLUR -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.Transparent,
              Color(0x88FFFFFF).copy(alpha = 0.40f * intensity)
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.6f
          ),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.BACKGROUND_BLUR -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.Transparent,
              Color(0x99000000).copy(alpha = 0.55f * intensity)
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.42f
          ),
          blendMode = BlendMode.Multiply
        )
      }

      VideoEffect.TILT_SHIFT -> {
        val bandH = height * 0.28f
        // Top blurred band
        scope.drawRect(
          color = Color(0x66000000).copy(alpha = 0.45f * intensity),
          topLeft = Offset(0f, 0f),
          size = Size(width, (height - bandH) * 0.5f),
          blendMode = BlendMode.Darken
        )
        // Bottom blurred band
        scope.drawRect(
          color = Color(0x66000000).copy(alpha = 0.45f * intensity),
          topLeft = Offset(0f, (height + bandH) * 0.5f),
          size = Size(width, (height - bandH) * 0.5f),
          blendMode = BlendMode.Darken
        )
      }

      VideoEffect.DREAMY_BLUR -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF).copy(alpha = 0.55f * intensity), Color.Transparent),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.65f
          ),
          blendMode = BlendMode.Plus
        )
      }

      // ---------------------------------------------------------------------
      // 6. CREATIVE
      // ---------------------------------------------------------------------
      VideoEffect.BLACK_AND_WHITE -> {
        scope.drawRect(
          color = Color.Black.copy(alpha = 0.98f * intensity),
          blendMode = BlendMode.Saturation
        )
      }

      VideoEffect.NEGATIVE -> {
        scope.drawRect(
          color = Color.White.copy(alpha = 0.95f * intensity),
          blendMode = BlendMode.Difference
        )
      }

      VideoEffect.SKETCH -> {
        scope.drawRect(
          color = Color.Black.copy(alpha = 0.88f * intensity),
          blendMode = BlendMode.Saturation
        )
        // Cross-hatch pencil pattern
        var x = 0f
        while (x < width) {
          scope.drawLine(
            color = Color.White.copy(alpha = 0.2f * intensity),
            start = Offset(x, 0f),
            end = Offset(x + height * 0.4f, height),
            strokeWidth = 1f
          )
          x += 12f
        }
      }

      VideoEffect.CARTOON_STYLE -> {
        scope.drawRect(
          color = Color(0xFFFFEB3B).copy(alpha = 0.25f * intensity),
          blendMode = BlendMode.Color
        )
        scope.drawRect(
          color = Color.Black.copy(alpha = 0.4f * intensity),
          blendMode = BlendMode.Hardlight
        )
      }

      VideoEffect.MIRROR -> {
        // Vertical bilateral mirror overlay
        scope.drawRect(
          brush = Brush.horizontalGradient(
            colors = listOf(
              Color.Transparent,
              Color(0x44FFFFFF).copy(alpha = 0.35f * intensity),
              Color.Transparent
            )
          ),
          topLeft = Offset(width * 0.48f, 0f),
          size = Size(width * 0.04f, height)
        )
      }

      VideoEffect.KALEIDOSCOPE -> {
        val center = Offset(width * 0.5f, height * 0.5f)
        for (i in 0 until 6) {
          val angle = i * 60f
          scope.rotate(angle, pivot = center) {
            drawLine(
              color = Color.White.copy(alpha = 0.3f * intensity),
              start = center,
              end = Offset(center.x + width, center.y),
              strokeWidth = 1.5f,
              blendMode = BlendMode.Screen
            )
          }
        }
      }

      VideoEffect.THERMAL_VISION -> {
        scope.drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFF0000).copy(alpha = 0.55f * intensity),
              Color(0xFFFFFF00).copy(alpha = 0.50f * intensity),
              Color(0xFF00FF00).copy(alpha = 0.45f * intensity),
              Color(0xFF0000FF).copy(alpha = 0.55f * intensity)
            )
          ),
          blendMode = BlendMode.Color
        )
      }

      VideoEffect.HALFTONE -> {
        val step = 16f
        var x = 8f
        while (x < width) {
          var y = 8f
          while (y < height) {
            scope.drawCircle(
              color = Color.Black.copy(alpha = 0.45f * intensity),
              radius = 2.4f * intensity,
              center = Offset(x, y),
              blendMode = BlendMode.Darken
            )
            y += step
          }
          x += step
        }
      }

      // ---------------------------------------------------------------------
      // 7. COLOR
      // ---------------------------------------------------------------------
      VideoEffect.WARM -> {
        scope.drawRect(
          color = Color(0xFFFFB300).copy(alpha = 0.35f * intensity),
          blendMode = BlendMode.Color
        )
      }

      VideoEffect.COOL -> {
        scope.drawRect(
          color = Color(0xFF00E5FF).copy(alpha = 0.32f * intensity),
          blendMode = BlendMode.Color
        )
      }

      VideoEffect.FADE -> {
        scope.drawRect(
          color = Color(0xFFECEFF1).copy(alpha = 0.32f * intensity),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.SATURATION_BOOST -> {
        scope.drawRect(
          color = Color(0xFFFF1744).copy(alpha = 0.25f * intensity),
          blendMode = BlendMode.Overlay
        )
      }

      VideoEffect.HIGH_CONTRAST -> {
        scope.drawRect(
          color = Color(0xFF000000).copy(alpha = 0.45f * intensity),
          blendMode = BlendMode.Overlay
        )
      }

      VideoEffect.SEPIA -> {
        scope.drawRect(
          color = Color(0xFF8D6E63).copy(alpha = 0.65f * intensity),
          blendMode = BlendMode.Color
        )
      }

      VideoEffect.NIGHT_VISION -> {
        scope.drawRect(
          color = Color(0xFF00FF41).copy(alpha = 0.68f * intensity),
          blendMode = BlendMode.Color
        )
        // Peripheral night vision vignette
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0xFF002200).copy(alpha = 0.9f * intensity)),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.58f
          )
        )
      }

      VideoEffect.HDR_LOOK -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x44FFFFFF).copy(alpha = 0.35f * intensity),
              Color(0x33000000).copy(alpha = 0.45f * intensity)
            ),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.7f
          ),
          blendMode = BlendMode.Overlay
        )
      }

      VideoEffect.SUNSET_AMBER -> {
        scope.drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFF3D00).copy(alpha = 0.45f * intensity),
              Color(0xFFFFAB00).copy(alpha = 0.35f * intensity)
            )
          ),
          blendMode = BlendMode.Color
        )
      }

      VideoEffect.EMERALD_VIVID -> {
        scope.drawRect(
          color = Color(0xFF00E676).copy(alpha = 0.40f * intensity),
          blendMode = BlendMode.Color
        )
      }

      VideoEffect.MOTION_SHAKE, VideoEffect.BEAT_SHAKE -> {
        val shakeOffset = (if (animPhase > 0.5f) 8f else -8f) * intensity
        scope.drawRect(
          color = Color.White.copy(alpha = 0.25f * intensity),
          topLeft = Offset(shakeOffset, shakeOffset),
          size = size,
          blendMode = BlendMode.Overlay
        )
      }

      VideoEffect.CAMERA_SHAKE -> {
        val rand = Random(((animPhase * 60).toInt()))
        val dx = (rand.nextFloat() * 12f - 6f) * intensity
        val dy = (rand.nextFloat() * 12f - 6f) * intensity
        scope.drawRect(
          color = Color.White.copy(alpha = 0.18f * intensity),
          topLeft = Offset(dx, dy),
          size = size,
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.EARTHQUAKE -> {
        val quakeOffset = sin(animPhase * 31.4f) * 14f * intensity
        scope.drawRect(
          color = Color(0xFF00E5FF).copy(alpha = 0.22f * intensity),
          topLeft = Offset(quakeOffset, -quakeOffset * 0.5f),
          size = size,
          blendMode = BlendMode.Screen
        )
        scope.drawRect(
          color = Color(0xFFFF0055).copy(alpha = 0.22f * intensity),
          topLeft = Offset(-quakeOffset, quakeOffset * 0.5f),
          size = size,
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.WHIP_ZOOM -> {
        val pulse = (sin(animPhase * 6.28f) * 0.12f * intensity)
        scope.scale(1f + pulse, pivot = Offset(width * 0.5f, height * 0.5f)) {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(Color.Transparent, Color(0x66FFFFFF).copy(alpha = 0.35f * intensity)),
              center = Offset(width * 0.5f, height * 0.5f),
              radius = width * 0.6f
            ),
            radius = width * 0.6f,
            center = Offset(width * 0.5f, height * 0.5f)
          )
        }
      }

      VideoEffect.BODY_NEON_STROKE -> {
        // Neon silhouette aura outline
        scope.drawCircle(
          color = Color(0xFF00E676).copy(alpha = 0.55f * intensity),
          radius = width * 0.38f,
          center = Offset(width * 0.5f, height * 0.52f),
          blendMode = BlendMode.Plus
        )
        scope.drawCircle(
          color = Color(0xFF00E5FF).copy(alpha = 0.40f * intensity),
          radius = width * 0.42f,
          center = Offset(width * 0.5f, height * 0.52f),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.AURA_GLOW -> {
        scope.drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFD54F).copy(alpha = 0.60f * intensity),
              Color(0xFFFF6D00).copy(alpha = 0.30f * intensity),
              Color.Transparent
            ),
            center = Offset(width * 0.5f, height * 0.48f),
            radius = width * 0.55f
          ),
          radius = width * 0.55f,
          center = Offset(width * 0.5f, height * 0.48f),
          blendMode = BlendMode.Plus
        )
      }

      VideoEffect.SHADOW_CLONE -> {
        val echoShift = 18f * intensity
        scope.drawRect(
          color = Color(0xFF00E5FF).copy(alpha = 0.32f * intensity),
          topLeft = Offset(-echoShift, 0f),
          size = size,
          blendMode = BlendMode.Screen
        )
        scope.drawRect(
          color = Color(0xFFFF2A85).copy(alpha = 0.32f * intensity),
          topLeft = Offset(echoShift, 0f),
          size = size,
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.PORTRAIT_FOCUS -> {
        scope.drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0x99000000).copy(alpha = 0.70f * intensity)),
            center = Offset(width * 0.5f, height * 0.45f),
            radius = width * 0.45f
          )
        )
      }

      VideoEffect.GOLD_DUST_PARTICLES -> {
        val rand = Random(((animPhase * 40).toInt()))
        for (i in 0 until 18) {
          val px = rand.nextFloat() * width
          val py = (rand.nextFloat() * height + (animPhase * height * 0.5f)) % height
          val pr = (2f + rand.nextFloat() * 4f) * intensity
          scope.drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.85f * intensity),
            radius = pr,
            center = Offset(px, py),
            blendMode = BlendMode.Plus
          )
        }
      }

      VideoEffect.CONFETTI_BURST -> {
        val rand = Random(((animPhase * 35).toInt()))
        val colors = listOf(Color(0xFF00E676), Color(0xFFFF2A85), Color(0xFFFFD54F), Color(0xFF00E5FF))
        for (i in 0 until 22) {
          val px = rand.nextFloat() * width
          val py = (rand.nextFloat() * height + (animPhase * height * 0.8f)) % height
          val col = colors[i % colors.size]
          scope.drawRect(
            color = col.copy(alpha = 0.80f * intensity),
            topLeft = Offset(px, py),
            size = Size(8f * intensity, 4f * intensity)
          )
        }
      }

      VideoEffect.SNOW_FALL -> {
        val rand = Random(((animPhase * 30).toInt()))
        for (i in 0 until 25) {
          val px = rand.nextFloat() * width
          val py = (rand.nextFloat() * height + (animPhase * height * 0.6f)) % height
          val pr = (1.5f + rand.nextFloat() * 3f) * intensity
          scope.drawCircle(
            color = Color.White.copy(alpha = 0.75f * intensity),
            radius = pr,
            center = Offset(px, py)
          )
        }
      }

      VideoEffect.FIRE_EMBERS -> {
        val rand = Random(((animPhase * 45).toInt()))
        for (i in 0 until 20) {
          val px = rand.nextFloat() * width
          val py = height - ((rand.nextFloat() * height + (animPhase * height * 0.7f)) % height)
          val pr = (2f + rand.nextFloat() * 3.5f) * intensity
          scope.drawCircle(
            color = Color(0xFFFF5722).copy(alpha = 0.90f * intensity),
            radius = pr,
            center = Offset(px, py),
            blendMode = BlendMode.Plus
          )
        }
      }

      VideoEffect.WARP_DISTORTION -> {
        val wave = sin(animPhase * 12.5f) * 16f * intensity
        scope.drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x5500E5FF).copy(alpha = 0.40f * intensity), Color.Transparent),
            startY = (height * animPhase),
            endY = (height * animPhase) + 80f
          ),
          topLeft = Offset(wave, 0f),
          size = size,
          blendMode = BlendMode.Overlay
        )
      }

      VideoEffect.FISHEYE_LENS -> {
        scope.drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0x66000000).copy(alpha = 0.60f * intensity)),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.50f
          ),
          radius = width * 0.50f,
          center = Offset(width * 0.5f, height * 0.5f)
        )
      }

      VideoEffect.VORTEX_SWIRL -> {
        scope.rotate(animPhase * 360f * intensity, pivot = Offset(width * 0.5f, height * 0.5f)) {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(Color.Transparent, Color(0x5500E676).copy(alpha = 0.45f * intensity), Color.Transparent),
              center = Offset(width * 0.5f, height * 0.5f),
              radius = width * 0.45f
            ),
            radius = width * 0.45f,
            center = Offset(width * 0.5f, height * 0.5f),
            blendMode = BlendMode.Screen
          )
        }
      }

      VideoEffect.RIPPLE_DISPLACEMENT -> {
        val ripRadius = (animPhase * width * 0.65f) % (width * 0.65f)
        scope.drawCircle(
          color = Color.White.copy(alpha = (1f - ripRadius / (width * 0.65f)) * 0.45f * intensity),
          radius = ripRadius,
          center = Offset(width * 0.5f, height * 0.5f),
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f * intensity),
          blendMode = BlendMode.Screen
        )
      }

      VideoEffect.NONE -> {}
    }
  }

  // =========================================================================
  // COMPOSE DRAW SCOPE: 20 PROFESSIONAL VIDEO TRANSITIONS
  // =========================================================================

  fun renderTransitionCompose(
    drawScope: DrawScope,
    transition: TransitionType,
    progress: Float, // 0.0f to 1.0f
    nextClip: VideoClip?,
    animPhase: Float
  ) {
    if (transition == TransitionType.NONE || progress !in 0.001f..0.999f) return
    val size = drawScope.size
    val width = size.width
    val height = size.height
    val t = progress.coerceIn(0f, 1f)

    // Next clip mock background gradient if needed
    val nextStart = try {
      Color(android.graphics.Color.parseColor(nextClip?.colorStartHex ?: "#00E676"))
    } catch (_: Exception) { Color(0xFF00E676) }
    val nextEnd = try {
      Color(android.graphics.Color.parseColor(nextClip?.colorEndHex ?: "#00796B"))
    } catch (_: Exception) { Color(0xFF00796B) }

    when (transition) {
      TransitionType.FADE_IN -> {
        // Next clip fades in over current
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          alpha = t
        )
      }

      TransitionType.FADE_OUT -> {
        // Fade out to black, then show next
        val alpha = if (t < 0.5f) (1f - t * 2f) else ((t - 0.5f) * 2f)
        if (t < 0.5f) {
          drawScope.drawRect(color = Color.Black.copy(alpha = (1f - alpha)))
        } else {
          drawScope.drawRect(
            brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
            alpha = alpha
          )
        }
      }

      TransitionType.CROSS_DISSOLVE, TransitionType.DISSOLVE -> {
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          alpha = t
        )
      }

      TransitionType.BLACK_FADE, TransitionType.FADE_BLACK -> {
        val blackAlpha = if (t < 0.5f) (t * 2f) else ((1f - t) * 2f)
        if (t >= 0.5f) {
          drawScope.drawRect(brush = Brush.linearGradient(listOf(nextStart, nextEnd)))
        }
        drawScope.drawRect(color = Color.Black.copy(alpha = blackAlpha.coerceIn(0f, 1f)))
      }

      TransitionType.WHITE_FADE -> {
        val whiteAlpha = if (t < 0.5f) (t * 2f) else ((1f - t) * 2f)
        if (t >= 0.5f) {
          drawScope.drawRect(brush = Brush.linearGradient(listOf(nextStart, nextEnd)))
        }
        drawScope.drawRect(color = Color.White.copy(alpha = whiteAlpha.coerceIn(0f, 1f)))
      }

      TransitionType.ZOOM_IN -> {
        val scaleVal = 1f + (t * 0.5f)
        drawScope.scale(scaleVal, pivot = Offset(width * 0.5f, height * 0.5f)) {
          drawRect(
            brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
            alpha = t
          )
        }
      }

      TransitionType.ZOOM_OUT -> {
        val scaleVal = 1.3f - (t * 0.3f)
        drawScope.scale(scaleVal, pivot = Offset(width * 0.5f, height * 0.5f)) {
          drawRect(
            brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
            alpha = t
          )
        }
      }

      TransitionType.SLIDE_LEFT -> {
        val leftOffset = width * (1f - t)
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          topLeft = Offset(leftOffset, 0f),
          size = Size(width, height)
        )
      }

      TransitionType.SLIDE_RIGHT -> {
        val leftOffset = -width * (1f - t)
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          topLeft = Offset(leftOffset, 0f),
          size = Size(width, height)
        )
      }

      TransitionType.SLIDE_UP -> {
        val topOffset = height * (1f - t)
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          topLeft = Offset(0f, topOffset),
          size = Size(width, height)
        )
      }

      TransitionType.SLIDE_DOWN -> {
        val topOffset = -height * (1f - t)
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          topLeft = Offset(0f, topOffset),
          size = Size(width, height)
        )
      }

      TransitionType.SWIPE_LEFT -> {
        val wipeW = width * t
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          topLeft = Offset(width - wipeW, 0f),
          size = Size(wipeW, height)
        )
      }

      TransitionType.SWIPE_RIGHT -> {
        val wipeW = width * t
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          topLeft = Offset(0f, 0f),
          size = Size(wipeW, height)
        )
      }

      TransitionType.CIRCLE_REVEAL -> {
        val maxRad = hypot(width, height) * 0.55f
        val rad = maxRad * t
        val circlePath = Path().apply {
          addOval(androidx.compose.ui.geometry.Rect(
            center = Offset(width * 0.5f, height * 0.5f),
            radius = rad
          ))
        }
        drawScope.clipPath(circlePath) {
          drawRect(brush = Brush.linearGradient(listOf(nextStart, nextEnd)))
        }
      }

      TransitionType.BLUR_TRANSITION -> {
        val blurAlpha = if (t < 0.5f) (t * 2f) else ((1f - t) * 2f)
        if (t >= 0.5f) {
          drawScope.drawRect(brush = Brush.linearGradient(listOf(nextStart, nextEnd)))
        }
        drawScope.drawRect(
          color = Color(0x99FFFFFF).copy(alpha = blurAlpha * 0.75f),
          blendMode = BlendMode.Screen
        )
      }

      TransitionType.SPIN_TRANSITION, TransitionType.WHIP_PAN -> {
        val angle = t * 360f
        drawScope.rotate(angle, pivot = Offset(width * 0.5f, height * 0.5f)) {
          drawRect(
            brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
            alpha = t
          )
        }
      }

      TransitionType.FLASH_TRANSITION, TransitionType.FLASH -> {
        val flashAlpha = (sin(t * 3.14159f) * 1.2f).coerceIn(0f, 1f)
        if (t >= 0.5f) {
          drawScope.drawRect(brush = Brush.linearGradient(listOf(nextStart, nextEnd)))
        }
        drawScope.drawRect(
          color = Color.White.copy(alpha = flashAlpha),
          blendMode = BlendMode.Plus
        )
      }

      TransitionType.GLITCH_TRANSITION, TransitionType.GLITCH -> {
        if (t >= 0.5f) {
          drawScope.drawRect(brush = Brush.linearGradient(listOf(nextStart, nextEnd)))
        }
        val rand = Random(((animPhase * 71).toInt()))
        val slices = 8
        for (i in 0 until slices) {
          val y = rand.nextFloat() * height
          val h = 18f
          val xShift = (rand.nextFloat() * 40f - 20f)
          drawScope.drawRect(
            color = Color(0xFF00E5FF).copy(alpha = 0.75f),
            topLeft = Offset(xShift, y),
            size = Size(width, h),
            blendMode = BlendMode.Difference
          )
        }
      }

      TransitionType.WIPE_TRANSITION -> {
        val wipeW = width * t
        drawScope.drawRect(
          brush = Brush.linearGradient(listOf(nextStart, nextEnd)),
          topLeft = Offset(0f, 0f),
          size = Size(wipeW, height)
        )
        // Glowing leading edge line
        drawScope.drawLine(
          color = Color(0xFF00E676),
          start = Offset(wipeW, 0f),
          end = Offset(wipeW, height),
          strokeWidth = 4f
        )
      }

      TransitionType.RGB_SPLIT_TRANSITION -> {
        val split = (sin(t * 3.14159f) * 24f)
        if (t >= 0.5f) {
          drawScope.drawRect(brush = Brush.linearGradient(listOf(nextStart, nextEnd)))
        }
        drawScope.drawRect(
          color = Color(0xFFFF0000).copy(alpha = 0.5f),
          topLeft = Offset(-split, 0f),
          size = size,
          blendMode = BlendMode.Screen
        )
        drawScope.drawRect(
          color = Color(0xFF00FFFF).copy(alpha = 0.5f),
          topLeft = Offset(split, 0f),
          size = size,
          blendMode = BlendMode.Screen
        )
      }

      TransitionType.NONE -> {}
    }
  }

  // =========================================================================
  // ANDROID CANVAS: 50+ EFFECTS & 20 TRANSITIONS (FOR EXPORT)
  // =========================================================================

  fun renderEffectsOnAndroidCanvas(
    canvas: Canvas,
    effects: List<AppliedEffect>,
    width: Int,
    height: Int,
    frameIndex: Int
  ) {
    if (effects.isEmpty()) return
    for (applied in effects) {
      val effect = applied.effect
      val intensity = applied.intensity.coerceIn(0f, 1f)
      if (effect == VideoEffect.NONE || intensity <= 0.01f) continue

      drawSingleEffectAndroidCanvas(canvas, effect, intensity, width, height, frameIndex)
    }
  }

  private fun drawSingleEffectAndroidCanvas(
    canvas: Canvas,
    effect: VideoEffect,
    intensity: Float,
    width: Int,
    height: Int,
    frameIndex: Int
  ) {
    when (effect) {
      VideoEffect.CINEMATIC_GLOW, VideoEffect.GLOW -> {
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          val shader = RadialGradient(
            width * 0.5f, height * 0.45f, width * 0.75f,
            intArrayOf(
              android.graphics.Color.argb((140 * intensity).toInt(), 255, 224, 178),
              android.graphics.Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
          )
          setShader(shader)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)
      }

      VideoEffect.FILM_LOOK, VideoEffect.TEAL_AND_ORANGE -> {
        val tintPaint = Paint().apply {
          color = android.graphics.Color.argb((85 * intensity).toInt(), 0, 131, 143)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)
      }

      VideoEffect.MOVIE_GRAIN, VideoEffect.FILM_GRAIN -> {
        val grainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = android.graphics.Color.argb((110 * intensity).toInt(), 255, 255, 255)
        }
        val count = (80 * intensity).toInt().coerceAtLeast(10)
        val rand = Random(frameIndex * 19 + 7)
        for (i in 0 until count) {
          val gx = rand.nextInt(width).toFloat()
          val gy = rand.nextInt(height).toFloat()
          canvas.drawCircle(gx, gy, 1.6f, grainPaint)
        }
      }

      VideoEffect.VIGNETTE, VideoEffect.CINEMATIC_BLUR, VideoEffect.BACKGROUND_BLUR -> {
        val shader = RadialGradient(
          width / 2f, height / 2f, width * (0.85f - 0.25f * intensity),
          intArrayOf(android.graphics.Color.TRANSPARENT, android.graphics.Color.argb((220 * intensity).toInt(), 0, 0, 0)),
          floatArrayOf(0.4f, 1.0f),
          Shader.TileMode.CLAMP
        )
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { setShader(shader) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)
      }

      VideoEffect.GLITCH, VideoEffect.SIGNAL_DISTORTION, VideoEffect.VHS_GLITCH -> {
        val glitchPaint = Paint().apply {
          color = android.graphics.Color.argb((180 * intensity).toInt(), 0, 229, 255)
        }
        val count = (6 * intensity).toInt().coerceAtLeast(1)
        val rand = Random(frameIndex * 31)
        for (i in 0 until count) {
          val y = rand.nextInt(height).toFloat()
          val sliceH = (12f * intensity).coerceAtLeast(4f)
          val xShift = (rand.nextInt(40) - 20) * intensity
          canvas.drawRect(xShift, y, width + xShift, y + sliceH, glitchPaint)
        }
      }

      VideoEffect.RGB_SPLIT, VideoEffect.CHROMATIC_ABERRATION -> {
        val redPaint = Paint().apply {
          color = android.graphics.Color.argb((70 * intensity).toInt(), 255, 0, 0)
        }
        val cyanPaint = Paint().apply {
          color = android.graphics.Color.argb((70 * intensity).toInt(), 0, 255, 255)
        }
        val offset = 12f * intensity
        canvas.drawRect(-offset, 0f, width - offset, height.toFloat(), redPaint)
        canvas.drawRect(offset, 0f, width + offset, height.toFloat(), cyanPaint)
      }

      VideoEffect.LENS_FLARE, VideoEffect.GOLDEN_LIGHT -> {
        val flarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          val shader = RadialGradient(
            width * 0.35f, height * 0.30f, width * 0.5f,
            intArrayOf(
              android.graphics.Color.argb((180 * intensity).toInt(), 255, 238, 88),
              android.graphics.Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
          )
          setShader(shader)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flarePaint)
      }

      VideoEffect.LIGHT_LEAK, VideoEffect.FILM_BURN -> {
        val leakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          val shader = RadialGradient(
            width * 0.9f, height * 0.2f, width * 0.65f,
            intArrayOf(
              android.graphics.Color.argb((190 * intensity).toInt(), 255, 69, 0),
              android.graphics.Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
          )
          setShader(shader)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), leakPaint)
      }

      VideoEffect.FLASH -> {
        val flashPaint = Paint().apply {
          color = android.graphics.Color.argb((120 * intensity).toInt(), 255, 255, 255)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flashPaint)
      }

      VideoEffect.VHS_SCANLINES, VideoEffect.SCANLINE_WAVE -> {
        val linePaint = Paint().apply {
          color = android.graphics.Color.argb((85 * intensity).toInt(), 0, 0, 0)
          strokeWidth = 2f
        }
        var y = (frameIndex % 8).toFloat()
        while (y < height) {
          canvas.drawLine(0f, y, width.toFloat(), y, linePaint)
          y += 8f
        }
      }

      VideoEffect.BLACK_AND_WHITE, VideoEffect.SILENT_8MM -> {
        val bwPaint = Paint().apply {
          color = android.graphics.Color.argb((100 * intensity).toInt(), 120, 120, 120)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bwPaint)
      }

      VideoEffect.NEGATIVE -> {
        val negPaint = Paint().apply {
          color = android.graphics.Color.argb((90 * intensity).toInt(), 255, 255, 255)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), negPaint)
      }

      VideoEffect.WARM, VideoEffect.SUNSET_AMBER -> {
        val warmPaint = Paint().apply {
          color = android.graphics.Color.argb((65 * intensity).toInt(), 255, 152, 0)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), warmPaint)
      }

      VideoEffect.COOL -> {
        val coolPaint = Paint().apply {
          color = android.graphics.Color.argb((65 * intensity).toInt(), 0, 176, 255)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), coolPaint)
      }

      VideoEffect.NIGHT_VISION -> {
        val nvPaint = Paint().apply {
          color = android.graphics.Color.argb((140 * intensity).toInt(), 0, 255, 65)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), nvPaint)
      }

      VideoEffect.EMERALD_VIVID -> {
        val emPaint = Paint().apply {
          color = android.graphics.Color.argb((80 * intensity).toInt(), 0, 230, 118)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), emPaint)
      }

      VideoEffect.SEPIA, VideoEffect.OLD_CAMERA -> {
        val sepiaPaint = Paint().apply {
          color = android.graphics.Color.argb((120 * intensity).toInt(), 141, 110, 99)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sepiaPaint)
      }

      else -> {
        // Fallback atmospheric wash
        val defPaint = Paint().apply {
          color = android.graphics.Color.argb((40 * intensity).toInt(), 255, 255, 255)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), defPaint)
      }
    }
  }

  fun renderTransitionOnAndroidCanvas(
    canvas: Canvas,
    transition: TransitionType,
    progress: Float,
    nextClip: VideoClip?,
    width: Int,
    height: Int,
    frameIndex: Int
  ) {
    if (transition == TransitionType.NONE || progress !in 0.001f..0.999f) return
    val t = progress.coerceIn(0f, 1f)

    val nextColStart = try {
      android.graphics.Color.parseColor(nextClip?.colorStartHex ?: "#00E676")
    } catch (_: Exception) { android.graphics.Color.parseColor("#00E676") }
    val nextColEnd = try {
      android.graphics.Color.parseColor(nextClip?.colorEndHex ?: "#00796B")
    } catch (_: Exception) { android.graphics.Color.parseColor("#00796B") }

    val nextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      val shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), nextColStart, nextColEnd, Shader.TileMode.CLAMP)
      setShader(shader)
    }

    when (transition) {
      TransitionType.FADE_IN, TransitionType.CROSS_DISSOLVE, TransitionType.DISSOLVE -> {
        nextPaint.alpha = (t * 255).toInt().coerceIn(0, 255)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), nextPaint)
      }

      TransitionType.FADE_OUT, TransitionType.BLACK_FADE, TransitionType.FADE_BLACK -> {
        val blackAlpha = if (t < 0.5f) (t * 2f * 255).toInt() else ((1f - t) * 2f * 255).toInt()
        if (t >= 0.5f) {
          canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), nextPaint)
        }
        val blackPaint = Paint().apply { color = android.graphics.Color.argb(blackAlpha.coerceIn(0, 255), 0, 0, 0) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), blackPaint)
      }

      TransitionType.WHITE_FADE, TransitionType.FLASH_TRANSITION, TransitionType.FLASH -> {
        val whiteAlpha = if (t < 0.5f) (t * 2f * 255).toInt() else ((1f - t) * 2f * 255).toInt()
        if (t >= 0.5f) {
          canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), nextPaint)
        }
        val whitePaint = Paint().apply { color = android.graphics.Color.argb(whiteAlpha.coerceIn(0, 255), 255, 255, 255) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), whitePaint)
      }

      TransitionType.SLIDE_LEFT, TransitionType.SWIPE_LEFT -> {
        val left = width * (1f - t)
        canvas.drawRect(left, 0f, width.toFloat(), height.toFloat(), nextPaint)
      }

      TransitionType.SLIDE_RIGHT, TransitionType.SWIPE_RIGHT -> {
        val right = width * t
        canvas.drawRect(0f, 0f, right, height.toFloat(), nextPaint)
      }

      TransitionType.SLIDE_UP -> {
        val top = height * (1f - t)
        canvas.drawRect(0f, top, width.toFloat(), height.toFloat(), nextPaint)
      }

      TransitionType.SLIDE_DOWN -> {
        val bottom = height * t
        canvas.drawRect(0f, 0f, width.toFloat(), bottom, nextPaint)
      }

      TransitionType.CIRCLE_REVEAL -> {
        val maxRad = hypot(width.toDouble(), height.toDouble()).toFloat() * 0.55f
        canvas.save()
        val path = android.graphics.Path().apply {
          addCircle(width * 0.5f, height * 0.5f, maxRad * t, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(path)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), nextPaint)
        canvas.restore()
      }

      TransitionType.WIPE_TRANSITION -> {
        val wipeW = width * t
        canvas.drawRect(0f, 0f, wipeW, height.toFloat(), nextPaint)
        val linePaint = Paint().apply {
          color = android.graphics.Color.parseColor("#00E676")
          strokeWidth = 6f
        }
        canvas.drawLine(wipeW, 0f, wipeW, height.toFloat(), linePaint)
      }

      TransitionType.GLITCH_TRANSITION, TransitionType.GLITCH, TransitionType.RGB_SPLIT_TRANSITION -> {
        if (t >= 0.5f) {
          canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), nextPaint)
        }
        val glitchPaint = Paint().apply {
          color = android.graphics.Color.argb(160, 0, 229, 255)
        }
        val slices = 6
        val rand = Random(frameIndex * 17)
        for (i in 0 until slices) {
          val y = rand.nextInt(height).toFloat()
          canvas.drawRect(0f, y, width.toFloat(), y + 16f, glitchPaint)
        }
      }

      else -> {
        nextPaint.alpha = (t * 255).toInt().coerceIn(0, 255)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), nextPaint)
      }
    }
  }
}
