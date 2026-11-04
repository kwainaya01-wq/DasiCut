package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TimelineView(
  project: Project,
  playheadMs: Long,
  selectedClipId: String?,
  selectedAudioId: String?,
  selectedTextId: String?,
  zoom: Float,
  onPlayheadChanged: (Long) -> Unit,
  onSelectClip: (String) -> Unit,
  onSelectAudio: (String) -> Unit,
  onSelectText: (String) -> Unit,
  onOpenTransitions: (String) -> Unit,
  onAddClipClick: () -> Unit,
  onSplitClick: () -> Unit,
  onZoomIn: () -> Unit,
  onZoomOut: () -> Unit,
  onReorderClips: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
  onTrimClip: (clipId: String, startMs: Long, endMs: Long) -> Unit = { _, _, _ -> },
  onReplaceClipClick: (clipId: String) -> Unit = {},
  onAudioMove: (audioId: String, newStartMs: Long) -> Unit = { _, _ -> },
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  // Base pixels per second (e.g. 50dp per second multiplied by zoom)
  val dpPerSecond = 48.dp * zoom
  val msToDp = { ms: Long -> (ms / 1000f) * dpPerSecond.value }

  val totalDuration = project.totalDurationMs.coerceAtLeast(3000L)
  val timelineWidthDp = (msToDp(totalDuration) + 160f).dp

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(CharcoalSurface)
      .border(BorderStroke(1.dp, CharcoalBorder))
  ) {
    // Timeline Toolbar (Zoom, Add Clip, Split, Time status)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        // Quick Split action
        FilledTonalButton(
          onClick = onSplitClick,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = CharcoalCard,
            contentColor = EmeraldPrimary
          ),
          modifier = Modifier.height(30.dp).testTag("timeline_split_button")
        ) {
          Icon(Icons.Default.ContentCut, contentDescription = "Split", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Split", style = MaterialTheme.typography.labelSmall)
        }

        // Add Clip
        FilledTonalButton(
          onClick = onAddClipClick,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = CharcoalCard,
            contentColor = TextPrimary
          ),
          modifier = Modifier.height(30.dp).testTag("timeline_add_clip_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Clip", modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add", style = MaterialTheme.typography.labelSmall)
        }

        // Clip-specific actions when a clip is selected (Reorder, Replace)
        val selectedIndex = project.clips.indexOfFirst { it.id == selectedClipId }
        if (selectedIndex >= 0) {
          if (selectedIndex > 0) {
            FilledTonalButton(
              onClick = { onReorderClips(selectedIndex, selectedIndex - 1) },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              colors = ButtonDefaults.filledTonalButtonColors(containerColor = CharcoalCard, contentColor = TextSecondary),
              modifier = Modifier.height(30.dp).testTag("timeline_move_left")
            ) {
              Icon(Icons.Default.ArrowBack, contentDescription = "Move Left", modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text("Move", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }
          }

          if (selectedIndex < project.clips.size - 1) {
            FilledTonalButton(
              onClick = { onReorderClips(selectedIndex, selectedIndex + 1) },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              colors = ButtonDefaults.filledTonalButtonColors(containerColor = CharcoalCard, contentColor = TextSecondary),
              modifier = Modifier.height(30.dp).testTag("timeline_move_right")
            ) {
              Text("Move", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
              Spacer(modifier = Modifier.width(2.dp))
              Icon(Icons.Default.ArrowForward, contentDescription = "Move Right", modifier = Modifier.size(12.dp))
            }
          }

          FilledTonalButton(
            onClick = { onReplaceClipClick(project.clips[selectedIndex].id) },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = CharcoalCard, contentColor = GoldAccent),
            modifier = Modifier.height(30.dp).testTag("timeline_replace_clip")
          ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "Replace", modifier = Modifier.size(14.dp), tint = GoldAccent)
            Spacer(modifier = Modifier.width(2.dp))
            Text("Replace", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
          }
        }
      }

      // Zoom Controls
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onZoomOut,
          modifier = Modifier.size(28.dp).background(CharcoalCard, CircleShape)
        ) {
          Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextSecondary, modifier = Modifier.size(14.dp))
        }
        Text(
          text = "${(zoom * 100).toInt()}%",
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted,
          modifier = Modifier.padding(horizontal = 2.dp)
        )
        IconButton(
          onClick = onZoomIn,
          modifier = Modifier.size(28.dp).background(CharcoalCard, CircleShape)
        ) {
          Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextSecondary, modifier = Modifier.size(14.dp))
        }
      }
    }

    // Scrollable Multi-Track Container with overlay Playhead
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .horizontalScroll(scrollState)
    ) {
      // Timeline content tracks
      Column(
        modifier = Modifier
          .width(timelineWidthDp)
          .fillMaxHeight()
          .pointerInput(timelineWidthDp, totalDuration) {
            detectTapGestures { offset ->
              val fraction = (offset.x / (timelineWidthDp.toPx() - 100)).coerceIn(0f, 1f)
              val targetTime = (fraction * totalDuration).toLong()
              onPlayheadChanged(targetTime)
            }
          }
      ) {
        // 1. Time Ruler
        TimeRuler(
          totalDurationMs = totalDuration,
          msToDp = msToDp,
          timelineWidthDp = timelineWidthDp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Video Clips Track
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          project.clips.forEachIndexed { index, clip ->
            val clipWidthDp = msToDp(clip.effectiveDurationMs).coerceAtLeast(40f).dp
            val isSelected = clip.id == selectedClipId

            // Clip Card
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = CharcoalCard,
              border = if (isSelected) BorderStroke(2.dp, EmeraldPrimary) else BorderStroke(1.dp, CharcoalBorder),
              modifier = Modifier
                .width(clipWidthDp)
                .fillMaxHeight()
                .clickable { onSelectClip(clip.id) }
                .testTag("clip_item_${clip.id}")
            ) {
              val startCol = try { Color(android.graphics.Color.parseColor(clip.colorStartHex)) } catch (_: Exception) { EmeraldDark }
              val endCol = try { Color(android.graphics.Color.parseColor(clip.colorEndHex)) } catch (_: Exception) { EmeraldPrimary }

              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Brush.horizontalGradient(listOf(startCol.copy(alpha = 0.5f), endCol.copy(alpha = 0.7f))))
                  .padding(horizontal = 6.dp, vertical = 4.dp)
              ) {
                // Left trim handle if selected
                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .align(Alignment.CenterStart)
                      .width(14.dp)
                      .fillMaxHeight()
                      .background(EmeraldPrimary, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                      .pointerInput(clip.id) {
                        detectDragGestures { change, dragAmount ->
                          change.consume()
                          val deltaMs = (dragAmount.x / dpPerSecond.value * 1000f * clip.speed).toLong()
                          val newStart = (clip.trimStartMs + deltaMs).coerceIn(0L, clip.trimEndMs - 200L)
                          onTrimClip(clip.id, newStart, clip.trimEndMs)
                        }
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Box(modifier = Modifier.width(2.dp).height(18.dp).background(CharcoalBackground))
                  }
                }

                // Title and duration
                Column(modifier = Modifier.align(Alignment.CenterStart).padding(start = if (isSelected) 18.dp else 4.dp)) {
                  Text(
                    text = clip.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                  )
                  Text(
                    text = "${(clip.effectiveDurationMs / 1000f).formatOneDecimal()}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp
                  )
                }

                // Speed / Filter / Effect indicator badge
                if (clip.speed != 1.0f || clip.filter != VideoFilter.ORIGINAL || clip.activeEffects.isNotEmpty()) {
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xAA000000),
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = if (isSelected) 16.dp else 2.dp)
                  ) {
                    val badgeText = when {
                      clip.activeEffects.isNotEmpty() -> "Fx ${clip.activeEffects.size}"
                      clip.filter != VideoFilter.ORIGINAL -> "Filter"
                      else -> "${clip.speed}x"
                    }
                    Text(
                      text = badgeText,
                      color = GoldAccent,
                      fontSize = 8.sp,
                      modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                  }
                }

                // Right trim handle if selected
                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .align(Alignment.CenterEnd)
                      .width(14.dp)
                      .fillMaxHeight()
                      .background(EmeraldPrimary, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                      .pointerInput(clip.id) {
                        detectDragGestures { change, dragAmount ->
                          change.consume()
                          val deltaMs = (dragAmount.x / dpPerSecond.value * 1000f * clip.speed).toLong()
                          val newEnd = (clip.trimEndMs + deltaMs).coerceIn(clip.trimStartMs + 200L, clip.durationMs)
                          onTrimClip(clip.id, clip.trimStartMs, newEnd)
                        }
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Box(modifier = Modifier.width(2.dp).height(18.dp).background(CharcoalBackground))
                  }
                }
              }
            }

            // Transition Icon between clips
            if (index < project.clips.size - 1) {
              Surface(
                shape = CircleShape,
                color = CharcoalSurfaceVariant,
                border = BorderStroke(1.dp, if (clip.transitionToNext != TransitionType.NONE) EmeraldPrimary else CharcoalBorder),
                modifier = Modifier
                  .padding(horizontal = 2.dp)
                  .size(22.dp)
                  .clickable { onOpenTransitions(clip.id) }
                  .testTag("transition_icon_${clip.id}")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = "Transition",
                    tint = if (clip.transitionToNext != TransitionType.NONE) EmeraldPrimary else TextMuted,
                    modifier = Modifier.size(12.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Audio Track (Multi-track with volume & drag to reposition)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .padding(horizontal = 16.dp)
        ) {
          if (project.audioTracks.isEmpty()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = CharcoalSurfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(1.dp, CharcoalBorder),
              modifier = Modifier.fillMaxSize()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tap + Audio to add background score or voiceover", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              }
            }
          } else {
            project.audioTracks.forEach { audio ->
              val audioOffsetDp = msToDp(audio.startAtMs).dp
              val audioWidthDp = msToDp(audio.durationMs).coerceAtLeast(60f).dp
              val isSelected = audio.id == selectedAudioId

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) CharcoalCardSelected else CharcoalCard,
                border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color(0xFF1E3A5F)),
                modifier = Modifier
                  .offset(x = audioOffsetDp)
                  .width(audioWidthDp)
                  .fillMaxHeight()
                  .pointerInput(audio.id) {
                    detectDragGestures { change, dragAmount ->
                      change.consume()
                      val deltaMs = (dragAmount.x / dpPerSecond.value * 1000f).toLong()
                      val newStart = (audio.startAtMs + deltaMs).coerceAtLeast(0L)
                      onAudioMove(audio.id, newStart)
                    }
                  }
                  .clickable { onSelectAudio(audio.id) }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0D3B66), Color(0xFF00A896))))
                    .padding(horizontal = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                      imageVector = if (audio.isMuted) Icons.Default.VolumeMute else Icons.Default.MusicNote,
                      contentDescription = null,
                      tint = if (audio.isMuted) TextMuted else GoldAccent,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${audio.title} (${(audio.durationMs / 1000f).toInt()}s)",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      maxLines = 1
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color(0x66000000)
                  ) {
                    Text(
                      text = if (audio.isMuted) "Mute" else "${(audio.volume * 100).toInt()}%",
                      fontSize = 8.sp,
                      color = if (audio.isMuted) TextMuted else EmeraldPrimary,
                      modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4. Text & Subtitle Track
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 16.dp)
        ) {
          project.textOverlays.forEach { textOverlay ->
            val textOffsetDp = msToDp(textOverlay.startAtMs).dp
            val textWidthDp = msToDp(textOverlay.durationMs).coerceAtLeast(40f).dp
            val isSelected = textOverlay.id == selectedTextId

            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (isSelected) CharcoalCardSelected else Color(0xFF2A1B3D),
              border = BorderStroke(1.dp, if (isSelected) NeonPink else Color(0xFF44318D)),
              modifier = Modifier
                .offset(x = textOffsetDp)
                .width(textWidthDp)
                .fillMaxHeight()
                .clickable { onSelectText(textOverlay.id) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.TextFields, contentDescription = null, tint = NeonPink, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = textOverlay.text,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // 5. Red Playhead Needle Overlay
      val playheadOffsetDp = (msToDp(playheadMs) + 16f).dp
      Box(
        modifier = Modifier
          .offset(x = playheadOffsetDp)
          .width(2.dp)
          .fillMaxHeight()
          .background(PlayheadRed)
          .pointerInput(timelineWidthDp, totalDuration) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              val currentX = msToDp(playheadMs) + dragAmount.x
              val fraction = (currentX / (timelineWidthDp.toPx() - 100)).coerceIn(0f, 1f)
              onPlayheadChanged((fraction * totalDuration).toLong())
            }
          }
      ) {
        // Playhead top marker bubble
        Box(
          modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = (-2).dp)
            .size(10.dp)
            .background(PlayheadRed, RoundedCornerShape(2.dp))
        )
      }
    }
  }
}

// Time ruler composable displaying second marks and tick indicators
@Composable
private fun TimeRuler(
  totalDurationMs: Long,
  msToDp: (Long) -> Float,
  timelineWidthDp: androidx.compose.ui.unit.Dp
) {
  val secondsCount = (totalDurationMs / 1000L).toInt() + 2

  Canvas(
    modifier = Modifier
      .width(timelineWidthDp)
      .height(18.dp)
      .padding(horizontal = 16.dp)
  ) {
    for (sec in 0..secondsCount) {
      val x = msToDp(sec * 1000L)
      // Major second mark
      drawLine(
        color = Color(0xFF64748B),
        start = Offset(x, size.height),
        end = Offset(x, size.height * 0.4f),
        strokeWidth = 1.5f
      )

      // Half second mark
      val halfX = msToDp((sec * 1000L) + 500L)
      drawLine(
        color = Color(0xFF475569),
        start = Offset(halfX, size.height),
        end = Offset(halfX, size.height * 0.7f),
        strokeWidth = 1.0f
      )
    }
  }
}

private fun Float.formatOneDecimal(): String {
  return "%.1f".format(this)
}
