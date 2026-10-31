package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun VideoPreviewCanvas(
  project: Project,
  playheadMs: Long,
  isPlaying: Boolean,
  previewQuality: String,
  onTogglePlay: () -> Unit,
  onStepBackward: () -> Unit,
  onStepForward: () -> Unit,
  onChangeRatioClick: () -> Unit,
  onChangeQualityClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isFullscreen by remember { mutableStateOf(false) }

  var clipStartMs = 0L
  var activeClip: VideoClip? = null
  var clipProgressMs = 0L

  for (c in project.clips) {
    val duration = c.effectiveDurationMs
    if (playheadMs in clipStartMs until (clipStartMs + duration)) {
      activeClip = c
      clipProgressMs = playheadMs - clipStartMs
      break
    }
    clipStartMs += duration
  }

  if (activeClip == null && project.clips.isNotEmpty()) {
    activeClip = project.clips.last()
    clipProgressMs = activeClip.effectiveDurationMs
  }

  val activeTexts = project.textOverlays.filter {
    playheadMs in it.startAtMs until (it.startAtMs + it.durationMs)
  }

  val activeStickers = project.stickers.filter {
    playheadMs in it.startAtMs until (it.startAtMs + it.durationMs)
  }

  val infiniteTransition = rememberInfiniteTransition(label = "previewAnim")
  val animPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "animPhase"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(CharcoalBackground),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder),
          modifier = Modifier
            .clickable { onChangeRatioClick() }
            .testTag("aspect_ratio_chip")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
            Text(project.aspectRatio.label, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder),
          modifier = Modifier
            .clickable { onChangeQualityClick() }
            .testTag("preview_quality_chip")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(EmeraldPrimary, CircleShape)
            )
            Text(previewQuality, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = CharcoalSurfaceVariant,
        modifier = Modifier.padding(2.dp)
      ) {
        Text(
          text = "${formatTimecode(playheadMs)} / ${formatTimecode(project.totalDurationMs)}",
          style = MaterialTheme.typography.labelMedium,
          color = EmeraldPrimary,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(if (isFullscreen) 360.dp else 220.dp)
        .background(Color.Black),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .aspectRatio(project.aspectRatio.ratio)
          .clip(RoundedCornerShape(6.dp))
          .border(1.dp, CharcoalBorder, RoundedCornerShape(6.dp))
          .background(Color.Black)
      ) {
        val videoUriString = activeClip?.uri
        if (!videoUriString.isNullOrEmpty()) {
          var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
          var isPrepared by remember { mutableStateOf(false) }

          AndroidView(
            factory = { ctx ->
              VideoView(ctx).apply {
                setOnPreparedListener { mp ->
                  mediaPlayerRef = mp
                  isPrepared = true
                  mp.setVolume(1.0f, 1.0f)
                  if (isPlaying) {
                    start()
                  }
                }
                setOnErrorListener { _, _, _ -> true }
              }
            },
            update = { videoView ->
              val targetUri = Uri.parse(videoUriString)
              if (videoView.tag != videoUriString) {
                videoView.tag = videoUriString
                isPrepared = false
                videoView.setVideoURI(targetUri)
              }

              if (isPrepared) {
                val seekTarget = ((activeClip?.startOffsetMs ?: 0L) + clipProgressMs).toInt()
                val currentPos = videoView.currentPosition
                if (abs(currentPos - seekTarget) > 250) {
                  videoView.seekTo(seekTarget)
                }

                if (isPlaying && !videoView.isPlaying) {
                  videoView.start()
                } else if (!isPlaying && videoView.isPlaying) {
                  videoView.pause()
                }
              }
            },
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawVideoFrame(
              clip = activeClip,
              clipProgressMs = clipProgressMs,
              animPhase = animPhase
            )
          }
        }

        if (activeClip != null && (activeClip.filter != VideoFilter.ORIGINAL || activeClip.effect != VideoEffect.NONE)) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            applyFilterAndEffects(
              filter = activeClip.filter,
              effect = activeClip.effect,
              animPhase = animPhase
            )
          }
        }

        activeTexts.forEach { textOverlay ->
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .offset(
                x = (textOverlay.positionX - 0.5f).dp * 200,
                y = (textOverlay.positionY - 0.5f).dp * 200
              )
              .padding(horizontal = 8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(android.graphics.Color.parseColor(textOverlay.bgColorHex ?: "#99000000")),
              border = if (textOverlay.hasStroke) BorderStroke(1.dp, Color(android.graphics.Color.parseColor(textOverlay.strokeColorHex))) else null,
              modifier = Modifier.padding(4.dp)
            ) {
              Text(
                text = textOverlay.text,
                color = Color(android.graphics.Color.parseColor(textOverlay.colorHex)),
                fontSize = (textOverlay.fontSizeSp * 0.7f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        activeStickers.forEach { sticker ->
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .offset(
                x = (sticker.positionX - 0.5f).dp * 200,
                y = (sticker.positionY - 0.5f).dp * 200
              )
          ) {
            Text(
              text = sticker.emojiOrSymbol,
              fontSize = (28 * sticker.scale).sp
            )
          }
        }
      }

      IconButton(
        onClick = { isFullscreen = !isFullscreen },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(8.dp)
          .size(32.dp)
          .background(Color(0x66000000), CircleShape)
      ) {
        Icon(
          imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
          contentDescription = "Toggle Fullscreen",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onStepBackward,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.SkipPrevious, contentDescription = "Step Backward", tint = TextSecondary)
      }

      Spacer(modifier = Modifier.width(16.dp))

      FilledIconButton(
        onClick = onTogglePlay,
        modifier = Modifier
          .size(48.dp)
          .testTag("play_pause_button"),
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = EmeraldPrimary,
          contentColor = CharcoalBackground
        )
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "Pause" else "Play",
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      IconButton(
        onClick = onStepForward,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.SkipNext, contentDescription = "Step Forward", tint = TextSecondary)
      }
    }
  }
}

private fun DrawScope.drawVideoFrame(
  clip: VideoClip?,
  clipProgressMs: Long,
  animPhase: Float
) {
  if (clip == null) {
    drawRect(Color.DarkGray)
    return
  }

  val startColor = try {
    Color(android.graphics.Color.parseColor(clip.colorStartHex))
  } catch (_: Exception) {
    EmeraldDark
  }

  val endColor = try {
    Color(android.graphics.Color.parseColor(clip.colorEndHex))
  } catch (_: Exception) {
    EmeraldPrimary
  }

  val progressFraction = (clipProgressMs.toFloat() / clip.effectiveDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
  val offsetX = (progressFraction - 0.5f) * size.width * 0.15f

  drawRect(
    brush = Brush.linearGradient(
      colors = listOf(startColor, endColor),
      start = Offset(offsetX, 0f),
      end = Offset(size.width + offsetX, size.height)
    )
  )
}

private fun DrawScope.applyFilterAndEffects(
  filter: VideoFilter,
  effect: VideoEffect,
  animPhase: Float
) {
  when (filter) {
    VideoFilter.ORIGINAL -> {}
    VideoFilter.WARM_DESI -> drawRect(color = Color(0x33FFB300), blendMode = BlendMode.Color)
    VideoFilter.BOLLYWOOD -> {
      drawRect(
        brush = Brush.verticalGradient(listOf(Color(0x33FF6D00), Color(0x440097A7))),
        blendMode = BlendMode.Overlay
      )
    }
    VideoFilter.COOL_TEAL -> drawRect(color = Color(0x3300E5FF), blendMode = BlendMode.Color)
    VideoFilter.VINTAGE_90S -> drawRect(color = Color(0x44BCAAA4), blendMode = BlendMode.Color)
    VideoFilter.MOODY_NOIR -> drawRect(color = Color(0x99000000), blendMode = BlendMode.Saturation)
    VideoFilter.EMERALD_DREAM -> drawRect(color = Color(0x4400E676), blendMode = BlendMode.Color)
    VideoFilter.CYBER_NEON -> {
      drawRect(
        brush = Brush.horizontalGradient(listOf(Color(0x44FF007F), Color(0x4400F0FF))),
        blendMode = BlendMode.Hardlight
      )
    }
    VideoFilter.SUNSET_CHAI -> drawRect(color = Color(0x44FF5722), blendMode = BlendMode.ColorBurn)
    VideoFilter.MONOCHROME -> drawRect(color = Color(0xEE000000), blendMode = BlendMode.Saturation)
  }

  when (effect) {
    VideoEffect.NONE -> {}
    VideoEffect.GLITCH -> {
      val glitchLineY = (animPhase * size.height) % size.height
      drawRect(color = Color(0x8800E5FF), topLeft = Offset(0f, glitchLineY), size = Size(size.width, 14f), blendMode = BlendMode.Screen)
      drawRect(color = Color(0x88FF0055), topLeft = Offset(10f, (glitchLineY + 24f) % size.height), size = Size(size.width - 20f, 8f), blendMode = BlendMode.Difference)
    }
    VideoEffect.RGB_SPLIT -> {
      drawRect(color = Color(0x33FF0000), topLeft = Offset(-6f, 0f), size = size, blendMode = BlendMode.Screen)
      drawRect(color = Color(0x3300FFFF), topLeft = Offset(6f, 0f), size = size, blendMode = BlendMode.Screen)
    }
    VideoEffect.FILM_GRAIN -> {
      val random = Random((animPhase * 100).toInt())
      for (i in 0..40) {
        val rx = random.nextFloat() * size.width
        val ry = random.nextFloat() * size.height
        drawCircle(color = Color(0x55FFFFFF), radius = 1.5f, center = Offset(rx, ry))
      }
    }
    VideoEffect.VHS_SCANLINES -> {
      var y = 0f
      while (y < size.height) {
        drawLine(color = Color(0x22000000), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 2f)
        y += 8f
      }
    }
    VideoEffect.VIGNETTE -> {
      drawRect(
        brush = Brush.radialGradient(listOf(Color.Transparent, Color(0xAA000000)), center = Offset(size.width * 0.5f, size.height * 0.5f), radius = size.width * 0.65f)
      )
    }
    VideoEffect.DREAMY_BLUR -> {
      drawRect(
        brush = Brush.radialGradient(listOf(Color(0x44FFFFFF), Color.Transparent), center = Offset(size.width * 0.5f, size.height * 0.5f), radius = size.width * 0.5f),
        blendMode = BlendMode.Plus
      )
    }
    VideoEffect.MOTION_SHAKE -> {
      val shakeOffset = if (animPhase > 0.5f) 4f else -4f
      drawRect(color = Color(0x22FFFFFF), topLeft = Offset(shakeOffset, shakeOffset), size = size, blendMode = BlendMode.Overlay)
    }
  }
}

fun formatTimecode(timeMs: Long): String {
  val totalSeconds = timeMs / 1000
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  val millisFirstDigit = (timeMs % 1000) / 100
  return "%02d:%02d.%d".format(minutes, seconds, millisFirstDigit)
}
