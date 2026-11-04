package com.example.media

import android.content.Context
import android.graphics.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.view.Surface
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sin
import kotlin.random.Random

object NativeVideoExporter {

  @Volatile
  private var isCancelled = false

  fun cancelExport() {
    isCancelled = true
  }

  suspend fun exportProject(
    context: Context,
    project: Project,
    settings: ExportSettings,
    onProgress: (progress: Float, stage: String) -> Unit
  ): File? = withContext(Dispatchers.Default) {
    isCancelled = false
    var encoder: MediaCodec? = null
    var muxer: MediaMuxer? = null
    var inputSurface: Surface? = null
    var outputFile: File? = null

    try {
      onProgress(0.05f, "Initializing encoder & workspace...")

      // Determine output video dimensions (must be even numbers for H.264)
      val (targetWidth, targetHeight) = calculateDimensions(project.aspectRatio, settings.resolution)
      val fps = settings.fps.coerceIn(24, 60)
      val totalDurationMs = project.totalDurationMs.coerceAtLeast(1000L)
      val frameDurationMs = (1000L / fps).coerceAtLeast(16L)
      val totalFrames = (totalDurationMs / frameDurationMs).toInt().coerceAtLeast(1)

      val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
      val cleanName = project.name.replace("[^a-zA-Z0-9_]".toRegex(), "_").take(18)
      outputFile = File(exportDir, "DesiCut_${cleanName}_${settings.resolution}.mp4")
      if (outputFile.exists()) {
        outputFile.delete()
      }

      val bitrate = when (settings.resolution) {
        "480p" -> 3_000_000
        "720p" -> 6_000_000
        "1080p" -> 12_000_000
        "4K" -> 20_000_000
        else -> 8_000_000
      }

      val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, targetWidth, targetHeight).apply {
        setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        setInteger(MediaFormat.KEY_FRAME_RATE, fps)
        setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
      }

      encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
      encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
      inputSurface = encoder.createInputSurface()
      encoder.start()

      muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
      var trackIndex = -1
      var muxerStarted = false
      val bufferInfo = MediaCodec.BufferInfo()

      onProgress(0.12f, "Encoding video frames & neural color grading...")

      // Frame rendering paint tools
      val clearPaint = Paint().apply { color = android.graphics.Color.BLACK }
      val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
      }
      val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        textAlign = Paint.Align.CENTER
      }
      val bgPillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
      }

      for (frameIndex in 0 until totalFrames) {
        if (isCancelled) {
          throw InterruptedException("Export cancelled by user")
        }

        val currentTimeMs = frameIndex * frameDurationMs
        val timestampUs = (frameIndex * 1_000_000L / fps)

        // Lock surface canvas
        val canvas: Canvas? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          inputSurface.lockHardwareCanvas()
        } else {
          inputSurface.lockCanvas(null)
        }

        if (canvas != null) {
          try {
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), clearPaint)

            // 1. Locate active clip and next clip for transitions
            var clipStartMs = 0L
            var activeClip: VideoClip? = null
            var activeClipIndex = -1
            var clipOffsetMs = 0L

            for (idx in project.clips.indices) {
              val clip = project.clips[idx]
              val clipDur = clip.effectiveDurationMs
              if (currentTimeMs in clipStartMs until (clipStartMs + clipDur)) {
                activeClip = clip
                activeClipIndex = idx
                clipOffsetMs = currentTimeMs - clipStartMs
                break
              }
              clipStartMs += clipDur
            }

            if (activeClip == null && project.clips.isNotEmpty()) {
              activeClip = project.clips.last()
              activeClipIndex = project.clips.size - 1
              clipOffsetMs = activeClip.effectiveDurationMs
            }

            if (activeClip != null) {
              val nextClip = if (activeClipIndex in 0 until (project.clips.size - 1)) project.clips[activeClipIndex + 1] else null
              val transDur = activeClip.transitionDurationMs.coerceAtLeast(100L)
              val transStartMs = activeClip.effectiveDurationMs - transDur
              val transProgress = if (activeClip.transitionToNext != TransitionType.NONE && nextClip != null && clipOffsetMs >= transStartMs) {
                ((clipOffsetMs - transStartMs).toFloat() / transDur).coerceIn(0f, 1f)
              } else 0f

              // Draw clip visual with real filters, 50+ effects stack, and 20 transitions
              drawClipToCanvas(
                canvas = canvas,
                clip = activeClip,
                clipOffsetMs = clipOffsetMs,
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                frameIndex = frameIndex,
                nextClip = nextClip,
                transitionProgress = transProgress
              )
            }

            // 2. Draw Active Text Overlays
            val activeTexts = project.textOverlays.filter {
              currentTimeMs in it.startAtMs until (it.startAtMs + it.durationMs)
            }
            for (textOverlay in activeTexts) {
              drawTextOverlayToCanvas(
                canvas = canvas,
                overlay = textOverlay,
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                textPaint = textPaint,
                strokePaint = strokePaint,
                bgPillPaint = bgPillPaint
              )
            }

            // 3. Draw Active Stickers
            val activeStickers = project.stickers.filter {
              currentTimeMs in it.startAtMs until (it.startAtMs + it.durationMs)
            }
            for (stk in activeStickers) {
              drawStickerToCanvas(
                canvas = canvas,
                sticker = stk,
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                paint = textPaint
              )
            }
          } finally {
            inputSurface.unlockCanvasAndPost(canvas)
          }
        }

        // Drain encoder output buffers
        while (true) {
          val encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 0)
          if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            break
          } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            if (muxerStarted) throw RuntimeException("Format changed after muxer started")
            trackIndex = muxer.addTrack(encoder.outputFormat)
            muxer.start()
            muxerStarted = true
          } else if (encoderStatus >= 0) {
            val encodedData = encoder.getOutputBuffer(encoderStatus)
            if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
              encodedData.position(bufferInfo.offset)
              encodedData.limit(bufferInfo.offset + bufferInfo.size)
              muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
            }
            encoder.releaseOutputBuffer(encoderStatus, false)
          }
        }

        val progressFraction = 0.12f + (frameIndex.toFloat() / totalFrames) * 0.76f
        if (frameIndex % 5 == 0 || frameIndex == totalFrames - 1) {
          onProgress(progressFraction, "Rendering frame $frameIndex / $totalFrames (${(progressFraction * 100).toInt()}%)...")
        }
      }

      // Signal end of stream
      encoder.signalEndOfInputStream()
      onProgress(0.92f, "Finalizing MP4 container & H.264 muxing...")

      // Drain remaining buffers until EOS
      var eosReached = false
      var retries = 0
      while (!eosReached && retries < 100) {
        val encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
        if (encoderStatus >= 0) {
          if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            eosReached = true
          }
          val encodedData = encoder.getOutputBuffer(encoderStatus)
          if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
            encodedData.position(bufferInfo.offset)
            encodedData.limit(bufferInfo.offset + bufferInfo.size)
            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
          }
          encoder.releaseOutputBuffer(encoderStatus, false)
        } else {
          retries++
        }
      }

      onProgress(1.0f, "Export Complete!")
      outputFile
    } catch (e: Exception) {
      e.printStackTrace()
      outputFile?.delete()
      null
    } finally {
      try { encoder?.stop() } catch (_: Exception) {}
      try { encoder?.release() } catch (_: Exception) {}
      try { inputSurface?.release() } catch (_: Exception) {}
      try {
        if (muxer != null) {
          muxer.stop()
          muxer.release()
        }
      } catch (_: Exception) {}
    }
  }

  private fun calculateDimensions(aspectRatio: AspectRatio, resolution: String): Pair<Int, Int> {
    val baseLong = when (resolution) {
      "480p" -> 854
      "720p" -> 1280
      "1080p" -> 1920
      "4K" -> 2160
      else -> 1280
    }

    return when (aspectRatio) {
      AspectRatio.RATIO_9_16 -> {
        val h = makeEven(baseLong)
        val w = makeEven((h * 9f / 16f).toInt())
        Pair(w, h)
      }
      AspectRatio.RATIO_16_9 -> {
        val w = makeEven(baseLong)
        val h = makeEven((w * 9f / 16f).toInt())
        Pair(w, h)
      }
      AspectRatio.RATIO_1_1 -> {
        val s = makeEven(minOf(baseLong, 1080))
        Pair(s, s)
      }
      AspectRatio.RATIO_4_5 -> {
        val h = makeEven(baseLong)
        val w = makeEven((h * 4f / 5f).toInt())
        Pair(w, h)
      }
      AspectRatio.RATIO_21_9 -> {
        val w = makeEven(baseLong)
        val h = makeEven((w * 9f / 21f).toInt())
        Pair(w, h)
      }
    }
  }

  private fun makeEven(value: Int): Int = if (value % 2 != 0) value + 1 else value

  private fun drawClipToCanvas(
    canvas: Canvas,
    clip: VideoClip,
    clipOffsetMs: Long,
    targetWidth: Int,
    targetHeight: Int,
    frameIndex: Int,
    nextClip: VideoClip? = null,
    transitionProgress: Float = 0f
  ) {
    val filterColorFilter = ColorFilterMatrixHelper.getAndroidGraphicsColorFilter(clip.filter, clip.filterIntensity)
    val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      colorFilter = filterColorFilter
    }

    // Check if clip has real video media
    val realBitmap = if (clip.localUri != null) {
      val sourceTimeMs = clip.trimStartMs + (clipOffsetMs * clip.speed).toLong()
      MediaFileManager.getFrameAtTime(clip.localUri, sourceTimeMs)
    } else null

    if (realBitmap != null) {
      // Scale and draw real imported video frame into target bounds
      val srcRect = Rect(0, 0, realBitmap.width, realBitmap.height)
      val destRect = Rect(0, 0, targetWidth, targetHeight)

      canvas.save()
      if (clip.rotationDegrees != 0) {
        canvas.rotate(clip.rotationDegrees.toFloat(), targetWidth / 2f, targetHeight / 2f)
      }
      if (clip.isFlippedH) {
        canvas.scale(-1f, 1f, targetWidth / 2f, targetHeight / 2f)
      }
      canvas.drawBitmap(realBitmap, srcRect, destRect, clipPaint)
      canvas.restore()
    } else {
      // Draw scenic background gradient
      val startCol = try { android.graphics.Color.parseColor(clip.colorStartHex) } catch (_: Exception) { android.graphics.Color.DKGRAY }
      val endCol = try { android.graphics.Color.parseColor(clip.colorEndHex) } catch (_: Exception) { android.graphics.Color.BLACK }

      val progress = (clipOffsetMs.toFloat() / clip.effectiveDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
      val shiftX = (progress - 0.5f) * targetWidth * 0.15f

      val shader = LinearGradient(
        shiftX, 0f, targetWidth + shiftX, targetHeight.toFloat(),
        startCol, endCol, Shader.TileMode.CLAMP
      )
      val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        setShader(shader)
        colorFilter = filterColorFilter
      }
      canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)

      // Scenic Sun and Silhouette
      val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#FFD54F")
        colorFilter = filterColorFilter
      }
      canvas.drawCircle(targetWidth * 0.5f + shiftX * 0.4f, targetHeight * 0.35f, targetWidth * 0.15f, sunPaint)

      val hillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#1B222D")
      }
      val path = android.graphics.Path().apply {
        moveTo(0f, targetHeight * 0.65f)
        cubicTo(
          targetWidth * 0.3f, targetHeight * 0.55f,
          targetWidth * 0.7f, targetHeight * 0.75f,
          targetWidth.toFloat(), targetHeight * 0.60f
        )
        lineTo(targetWidth.toFloat(), targetHeight.toFloat())
        lineTo(0f, targetHeight.toFloat())
        close()
      }
      canvas.drawPath(path, hillPaint)
    }

    // Apply 50+ Real Visual Effects across multi-effect stack
    VideoEffectsAndTransitionsRenderer.renderEffectsOnAndroidCanvas(
      canvas = canvas,
      effects = clip.activeEffects,
      width = targetWidth,
      height = targetHeight,
      frameIndex = frameIndex
    )

    // Apply 20 Professional Video Transitions
    if (transitionProgress > 0f && clip.transitionToNext != TransitionType.NONE) {
      VideoEffectsAndTransitionsRenderer.renderTransitionOnAndroidCanvas(
        canvas = canvas,
        transition = clip.transitionToNext,
        progress = transitionProgress,
        nextClip = nextClip,
        width = targetWidth,
        height = targetHeight,
        frameIndex = frameIndex
      )
    }
  }

  private fun drawTextOverlayToCanvas(
    canvas: Canvas,
    overlay: TextOverlay,
    targetWidth: Int,
    targetHeight: Int,
    textPaint: Paint,
    strokePaint: Paint,
    bgPillPaint: Paint
  ) {
    val x = overlay.positionX * targetWidth
    val y = overlay.positionY * targetHeight
    val text = overlay.text

    val scaledSize = overlay.fontSizeSp * (targetWidth / 400f)
    textPaint.textSize = scaledSize
    textPaint.color = try { android.graphics.Color.parseColor(overlay.colorHex) } catch (_: Exception) { android.graphics.Color.WHITE }

    // Background pill if present
    if (!overlay.bgColorHex.isNullOrBlank()) {
      val textBounds = Rect()
      textPaint.getTextBounds(text, 0, text.length, textBounds)
      val padX = scaledSize * 0.4f
      val padY = scaledSize * 0.25f

      val pillRect = RectF(
        x - textBounds.width() / 2f - padX,
        y - textBounds.height() - padY,
        x + textBounds.width() / 2f + padX,
        y + padY
      )
      bgPillPaint.color = try { android.graphics.Color.parseColor(overlay.bgColorHex) } catch (_: Exception) { android.graphics.Color.DKGRAY }
      canvas.drawRoundRect(pillRect, 12f, 12f, bgPillPaint)
    }

    // Stroke outline
    if (overlay.hasStroke) {
      strokePaint.textSize = scaledSize
      strokePaint.color = try { android.graphics.Color.parseColor(overlay.strokeColorHex) } catch (_: Exception) { android.graphics.Color.BLACK }
      strokePaint.strokeWidth = scaledSize * 0.1f
      canvas.drawText(text, x, y, strokePaint)
    }

    // Fill text
    canvas.drawText(text, x, y, textPaint)
  }

  private fun drawStickerToCanvas(
    canvas: Canvas,
    sticker: StickerOverlay,
    targetWidth: Int,
    targetHeight: Int,
    paint: Paint
  ) {
    val x = sticker.positionX * targetWidth
    val y = sticker.positionY * targetHeight
    paint.textSize = 52f * sticker.scale * (targetWidth / 400f)
    paint.color = android.graphics.Color.WHITE
    canvas.drawText(sticker.emojiOrSymbol, x, y, paint)
  }
}
