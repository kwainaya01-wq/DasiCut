package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.IntSize
import android.view.TextureView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.media.ColorFilterMatrixHelper
import com.example.media.MediaFileManager
import com.example.media.NativeVideoPlayerManager
import com.example.model.*
import com.example.ui.theme.*
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
  onUpdateTextPosition: (String, Float, Float) -> Unit = { _, _, _ -> },
  onSelectText: (String) -> Unit = {},
  playerManager: NativeVideoPlayerManager? = null,
  onToggleMute: () -> Unit = {},
  isRetouchBypass: Boolean = false,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activePlayerManager = playerManager ?: remember { NativeVideoPlayerManager(context.applicationContext) }

  var isFullscreen by remember { mutableStateOf(false) }

  // Find currently active clip and offset within clip
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

  // Active background music / audio track at current playhead
  val activeBgmTrack = project.audioTracks.find {
    playheadMs in it.startAtMs until (it.startAtMs + it.durationMs)
  }

  // Synchronize native player on state changes
  LaunchedEffect(activeClip?.id, clipProgressMs, isPlaying, activeClip?.volume, activeClip?.isMuted, activeClip?.speed) {
    activePlayerManager.syncWithTimeline(
      clip = activeClip,
      clipOffsetMs = clipProgressMs,
      isPlaying = isPlaying,
      activeAudioTrack = activeBgmTrack
    )
  }

  // Active text overlays at current playhead
  val activeTexts = project.textOverlays.filter {
    playheadMs in it.startAtMs until (it.startAtMs + it.durationMs)
  }

  // Active stickers at current playhead
  val activeStickers = project.stickers.filter {
    playheadMs in it.startAtMs until (it.startAtMs + it.durationMs)
  }

  // Pulse animation for live effect simulations (glitch, grain, scanlines)
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
    // Aspect Ratio & Quality Indicator Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        // Aspect ratio chip
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

        // Preview Quality chip
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

      // Timecode
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

    // Video Frame Box with constrained aspect ratio
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(if (isFullscreen) 360.dp else 220.dp)
        .background(Color.Black),
      contentAlignment = Alignment.Center
    ) {
      // Inner aspect-ratio box
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .aspectRatio(project.aspectRatio.ratio)
          .clip(RoundedCornerShape(6.dp))
          .border(1.dp, CharcoalBorder, RoundedCornerShape(6.dp))
          .background(Color.Black)
      ) {
        val hasRealVideo = activeClip?.localUri != null

        // 1. Hardware-accelerated native video surface for imported media
        if (hasRealVideo) {
          AndroidView(
            factory = { ctx ->
              TextureView(ctx).apply {
                activePlayerManager.attachTextureView(this)
              }
            },
            update = { view ->
              activePlayerManager.attachTextureView(view)
            },
            modifier = Modifier
              .fillMaxSize()
              .testTag("native_video_surface")
          )
        }

        // Calculate transition progress to next clip
        val currentClipIdx = project.clips.indexOfFirst { it.id == activeClip?.id }
        val nextClip = if (currentClipIdx in 0 until (project.clips.size - 1)) project.clips[currentClipIdx + 1] else null
        val transDuration = activeClip?.transitionDurationMs ?: 500L
        val transitionProgress = if (activeClip != null && activeClip.transitionToNext != TransitionType.NONE && nextClip != null) {
          val transStartMs = activeClip.effectiveDurationMs - transDuration
          if (clipProgressMs >= transStartMs) {
            ((clipProgressMs - transStartMs).toFloat() / transDuration.coerceAtLeast(100L)).coerceIn(0f, 1f)
          } else 0f
        } else 0f

        // 2. Main Canvas for scenic demo frame + real-time filters & 50+ effects & 20 transitions & Face Retouch
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawVideoOverlaysAndDemoFrame(
            clip = activeClip,
            clipProgressMs = clipProgressMs,
            animPhase = animPhase,
            isRealMediaActive = hasRealVideo,
            nextClip = nextClip,
            transitionProgress = transitionProgress,
            isRetouchBypass = isRetouchBypass
          )
        }

        // Text Overlays with interactive dragging
        activeTexts.forEach { textOverlay ->
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .offset(
                x = (textOverlay.positionX - 0.5f).dp * 260,
                y = (textOverlay.positionY - 0.5f).dp * 260
              )
              .pointerInput(textOverlay.id) {
                detectDragGestures(
                  onDragStart = { onSelectText(textOverlay.id) },
                  onDrag = { change, dragAmount ->
                    change.consume()
                    val newX = (textOverlay.positionX + dragAmount.x / 400f).coerceIn(0.08f, 0.92f)
                    val newY = (textOverlay.positionY + dragAmount.y / 400f).coerceIn(0.08f, 0.92f)
                    onUpdateTextPosition(textOverlay.id, newX, newY)
                  }
                )
              }
              .clickable { onSelectText(textOverlay.id) }
              .padding(4.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(android.graphics.Color.parseColor(textOverlay.bgColorHex ?: "#99000000")),
              border = if (textOverlay.hasStroke) BorderStroke(1.dp, Color(android.graphics.Color.parseColor(textOverlay.strokeColorHex))) else BorderStroke(1.dp, Color(0x33FFFFFF)),
              modifier = Modifier.padding(2.dp)
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

        // Active Stickers
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

        // Filter / Effect / Transition / Retouch active badge in corner
        if (activeClip != null && (activeClip.filter != VideoFilter.ORIGINAL || activeClip.activeEffects.isNotEmpty() || transitionProgress > 0f || activeClip.retouch.isEnabled)) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xCC000000),
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(6.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isRetouchBypass) {
                Text(
                  text = "BEFORE (ORIGINAL)",
                  style = MaterialTheme.typography.labelSmall,
                  color = CoralPink,
                  fontWeight = FontWeight.Bold
                )
              }
              if (activeClip.filter != VideoFilter.ORIGINAL) {
                Text(
                  text = activeClip.filter.displayName,
                  style = MaterialTheme.typography.labelSmall,
                  color = GoldAccent
                )
              }
              if (activeClip.activeEffects.isNotEmpty()) {
                val fxSummary = if (activeClip.activeEffects.size == 1) {
                  "• ${activeClip.activeEffects.first().effect.displayName}"
                } else {
                  "• ${activeClip.activeEffects.first().effect.displayName} (+${activeClip.activeEffects.size - 1})"
                }
                Text(
                  text = fxSummary,
                  style = MaterialTheme.typography.labelSmall,
                  color = EmeraldPrimary
                )
              }
              if (activeClip.retouch.isEnabled && !isRetouchBypass) {
                Text(
                  text = "✨ Retouch ${(activeClip.retouch.overallIntensity * 100).toInt()}%",
                  style = MaterialTheme.typography.labelSmall,
                  color = CoralPink,
                  fontWeight = FontWeight.Bold
                )
              }
              if (transitionProgress > 0f && activeClip.transitionToNext != TransitionType.NONE) {
                Text(
                  text = "⚡ ${activeClip.transitionToNext.displayName}",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF00E5FF)
                )
              }
            }
          }
        }
      }

      // Fullscreen toggle button
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

    // Playback control buttons row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Step back 1 frame (-100ms)
      IconButton(
        onClick = onStepBackward,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.SkipPrevious, contentDescription = "Step Backward", tint = TextSecondary)
      }

      Spacer(modifier = Modifier.width(16.dp))

      // Main Play / Pause glowing pill
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

      // Step forward 1 frame (+100ms)
      IconButton(
        onClick = onStepForward,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.SkipNext, contentDescription = "Step Forward", tint = TextSecondary)
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Mute / Unmute quick toggle button
      val isClipMuted = activeClip?.isMuted == true
      IconButton(
        onClick = onToggleMute,
        modifier = Modifier
          .size(36.dp)
          .testTag("preview_mute_toggle")
      ) {
        Icon(
          imageVector = if (isClipMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
          contentDescription = if (isClipMuted) "Unmute Audio" else "Mute Audio",
          tint = if (isClipMuted) ErrorRed else EmeraldPrimary
        )
      }
    }
  }
}

// Canvas rendering helper for real-time video filter overlays, visual effects, and demo frame gradients
private fun DrawScope.drawVideoOverlaysAndDemoFrame(
  clip: VideoClip?,
  clipProgressMs: Long,
  animPhase: Float,
  isRealMediaActive: Boolean,
  nextClip: VideoClip? = null,
  transitionProgress: Float = 0f,
  isRetouchBypass: Boolean = false
) {
  if (clip == null) {
    drawRect(Color.DarkGray)
    return
  }

  // 1. Draw rich scenic canvas background only when no real media is playing
  if (!isRealMediaActive) {
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

    // Visual simulation of frame animation based on clipProgress
    val progressFraction = (clipProgressMs.toFloat() / clip.effectiveDurationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
    val offsetX = (progressFraction - 0.5f) * size.width * 0.15f

    // Base background gradient representing the clip content
    drawRect(
      brush = Brush.linearGradient(
        colors = listOf(startColor, endColor),
        start = Offset(offsetX, 0f),
        end = Offset(size.width + offsetX, size.height)
      )
    )

    // Decorative scenic elements (sun/moon, waves, silhouette)
    val sunCenter = Offset(size.width * 0.5f + (offsetX * 0.5f), size.height * 0.35f)
    drawCircle(
      color = Color(0x66FFFFFF),
      radius = size.width * 0.18f,
      center = sunCenter
    )
    drawCircle(
      color = GoldAccent.copy(alpha = 0.8f),
      radius = size.width * 0.12f,
      center = sunCenter
    )

    val path = Path().apply {
      moveTo(0f, size.height * 0.65f)
      cubicTo(
        size.width * 0.25f, size.height * 0.55f,
        size.width * 0.75f, size.height * 0.75f,
        size.width, size.height * 0.60f
      )
      lineTo(size.width, size.height)
      lineTo(0f, size.height)
      close()
    }
    drawPath(path, color = Color(0x44000000))

    val fgPath = Path().apply {
      moveTo(0f, size.height * 0.82f)
      cubicTo(
        size.width * 0.35f, size.height * 0.75f,
        size.width * 0.65f, size.height * 0.88f,
        size.width, size.height * 0.80f
      )
      lineTo(size.width, size.height)
      lineTo(0f, size.height)
      close()
    }
    drawPath(fgPath, color = Color(0x88000000))
  }

  // 2. Real Filter Processing with intensity scaling
  val intensity = clip.filterIntensity.coerceIn(0f, 1f)
  if (clip.filter != VideoFilter.ORIGINAL && intensity > 0.01f) {
    when (clip.filter) {
      VideoFilter.WARM_DESI -> {
        drawRect(color = Color(0xFFFFB300).copy(alpha = 0.35f * intensity), blendMode = BlendMode.Color)
      }
      VideoFilter.BOLLYWOOD -> {
        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF6D00).copy(alpha = 0.35f * intensity), Color(0xFF0097A7).copy(alpha = 0.45f * intensity))
          ),
          blendMode = BlendMode.Overlay
        )
      }
      VideoFilter.COOL_TEAL -> {
        drawRect(color = Color(0xFF00E5FF).copy(alpha = 0.35f * intensity), blendMode = BlendMode.Color)
      }
      VideoFilter.VINTAGE_90S -> {
        drawRect(color = Color(0xFFBCAAA4).copy(alpha = 0.45f * intensity), blendMode = BlendMode.Color)
      }
      VideoFilter.MOODY_NOIR -> {
        drawRect(color = Color(0xFF000000).copy(alpha = 0.95f * intensity), blendMode = BlendMode.Saturation)
      }
      VideoFilter.EMERALD_DREAM -> {
        drawRect(color = Color(0xFF00E676).copy(alpha = 0.45f * intensity), blendMode = BlendMode.Color)
      }
      VideoFilter.CYBER_NEON -> {
        drawRect(
          brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFF007F).copy(alpha = 0.45f * intensity), Color(0xFF00F0FF).copy(alpha = 0.45f * intensity))
          ),
          blendMode = BlendMode.Hardlight
        )
      }
      VideoFilter.SUNSET_CHAI -> {
        drawRect(color = Color(0xFFFF5722).copy(alpha = 0.45f * intensity), blendMode = BlendMode.ColorBurn)
      }
      VideoFilter.MONOCHROME -> {
        drawRect(color = Color(0xFF000000).copy(alpha = 0.98f * intensity), blendMode = BlendMode.Saturation)
      }
      VideoFilter.ORIGINAL -> {}
    }
  }

  // 3. Multi-Effect Stack Processing (renders all 50+ real effects)
  com.example.media.VideoEffectsAndTransitionsRenderer.renderEffectsOnCompose(
    drawScope = this,
    effects = clip.activeEffects,
    animPhase = animPhase
  )

  // 4. Professional Video Transitions (20 transitions)
  if (transitionProgress > 0f && clip.transitionToNext != TransitionType.NONE) {
    com.example.media.VideoEffectsAndTransitionsRenderer.renderTransitionCompose(
      drawScope = this,
      transition = clip.transitionToNext,
      progress = transitionProgress,
      nextClip = nextClip,
      animPhase = animPhase
    )
  }

  // 5. Professional Face Retouch Layer
  if (clip.retouch.isEnabled && !isRetouchBypass) {
    com.example.media.FaceRetouchProcessor.renderRetouchCompose(
      drawScope = this,
      settings = clip.retouch,
      animPhase = animPhase
    )
  }

  // Freeze frame badge indicator
  if (clip.isFrozen) {
    drawRect(
      color = Color(0x2200E5FF),
      size = size
    )
  }
}

fun formatTimecode(timeMs: Long): String {
  val totalSeconds = timeMs / 1000
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  val millisFirstDigit = (timeMs % 1000) / 100
  return "%02d:%02d.%d".format(minutes, seconds, millisFirstDigit)
}
