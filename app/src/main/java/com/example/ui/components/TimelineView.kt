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
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*

sealed class TimelineTarget {
  data class Clip(val clip: VideoClip) : TimelineTarget()
  data class Audio(val track: AudioTrack) : TimelineTarget()
  data class Text(val overlay: TextOverlay) : TimelineTarget()
  data class Sticker(val sticker: StickerOverlay) : TimelineTarget()
}

@Composable
fun TimelineView(
  project: Project,
  playheadMs: Long,
  selectedClipId: String?,
  selectedAudioId: String?,
  selectedTextId: String?,
  selectedStickerId: String? = null,
  zoom: Float,
  onPlayheadChanged: (Long) -> Unit,
  onSelectClip: (String) -> Unit,
  onSelectAudio: (String) -> Unit,
  onSelectText: (String) -> Unit,
  onSelectSticker: (String) -> Unit = {},
  onOpenTransitions: (String) -> Unit,
  onAddClipClick: () -> Unit,
  onSplitClick: () -> Unit,
  onZoomIn: () -> Unit,
  onZoomOut: () -> Unit,
  onReorderClips: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
  onTrimClip: (clipId: String, startMs: Long, endMs: Long) -> Unit = { _, _, _ -> },
  onReplaceClipClick: (clipId: String) -> Unit = {},
  onDuplicateClip: (clipId: String) -> Unit = {},
  onDeleteClip: (clipId: String) -> Unit = {},
  onAudioMove: (audioId: String, newStartMs: Long) -> Unit = { _, _ -> },
  onDuplicateAudio: (audioId: String) -> Unit = {},
  onDeleteAudio: (audioId: String) -> Unit = {},
  onTextMove: (textId: String, newStartMs: Long) -> Unit = { _, _ -> },
  onTrimText: (textId: String, newStartMs: Long, newDurationMs: Long) -> Unit = { _, _, _ -> },
  onDuplicateText: (textId: String) -> Unit = {},
  onDeleteText: (textId: String) -> Unit = {},
  onStickerMove: (stickerId: String, newStartMs: Long) -> Unit = { _, _ -> },
  onTrimSticker: (stickerId: String, newStartMs: Long, newDurationMs: Long) -> Unit = { _, _, _ -> },
  onDuplicateSticker: (stickerId: String) -> Unit = {},
  onDeleteSticker: (stickerId: String) -> Unit = {},
  onEditText: (textId: String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  // Contextual Long-Press Target Dialog State
  var longPressTarget by remember { mutableStateOf<TimelineTarget?>(null) }

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
    // 1. Timeline Toolbar (Zoom, Add Clip, Split, Reorder, Replace)
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

        // Clip-specific actions when a clip is selected
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
            }
          }

          if (selectedIndex < project.clips.size - 1) {
            FilledTonalButton(
              onClick = { onReorderClips(selectedIndex, selectedIndex + 1) },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              colors = ButtonDefaults.filledTonalButtonColors(containerColor = CharcoalCard, contentColor = TextSecondary),
              modifier = Modifier.height(30.dp).testTag("timeline_move_right")
            ) {
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

      // Timecode and Zoom
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        val currentSec = (playheadMs / 1000f).formatOneDecimal()
        val totalSec = (totalDuration / 1000f).formatOneDecimal()
        Text(
          text = "${currentSec}s / ${totalSec}s",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary,
          modifier = Modifier.padding(end = 4.dp)
        )

        IconButton(onClick = onZoomOut, modifier = Modifier.size(26.dp).testTag("timeline_zoom_out")) {
          Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Zoom Out", tint = TextMuted, modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onZoomIn, modifier = Modifier.size(26.dp).testTag("timeline_zoom_in")) {
          Icon(Icons.Default.AddCircleOutline, contentDescription = "Zoom In", tint = TextMuted, modifier = Modifier.size(16.dp))
        }
      }
    }

    // 2. Multi-Track Timeline Tracks Area (Scrollable horizontal view)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .horizontalScroll(scrollState)
    ) {
      Column(
        modifier = Modifier
          .width(timelineWidthDp)
          .fillMaxHeight()
          .padding(vertical = 4.dp)
      ) {
        // Time Ruler
        TimeRuler(totalDurationMs = totalDuration, msToDp = msToDp, timelineWidthDp = timelineWidthDp)

        Spacer(modifier = Modifier.height(4.dp))

        // Track 1: [V] Video Track (Clips, transitions, trim handles)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Track header label
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0x3300E676),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.padding(end = 6.dp)
          ) {
            Text("V", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
          }

          project.clips.forEachIndexed { index, clip ->
            val clipWidthDp = msToDp(clip.effectiveDurationMs).coerceAtLeast(48f).dp
            val isSelected = clip.id == selectedClipId

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) CharcoalCardSelected else CharcoalCard,
              border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) EmeraldPrimary else CharcoalBorder),
              modifier = Modifier
                .width(clipWidthDp)
                .fillMaxHeight()
                .pointerInput(clip.id) {
                  detectTapGestures(
                    onTap = { onSelectClip(clip.id) },
                    onLongPress = { longPressTarget = TimelineTarget.Clip(clip) }
                  )
                }
                .testTag("timeline_clip_${clip.id}")
            ) {
              Box(modifier = Modifier.fillMaxSize()) {
                // Gradient thumbnail background
                val startColor = try { Color(android.graphics.Color.parseColor(clip.colorStartHex)) } catch (_: Exception) { Color(0xFF00B09B) }
                val endColor = try { Color(android.graphics.Color.parseColor(clip.colorEndHex)) } catch (_: Exception) { Color(0xFF96C93D) }

                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(startColor.copy(alpha = 0.65f), endColor.copy(alpha = 0.65f))))
                )

                // Info text (Title, Speed, Duration)
                Column(
                  modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = clip.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                  )
                  Text(
                    text = "${(clip.effectiveDurationMs / 1000f).formatOneDecimal()}s" + if (clip.speed != 1.0f) " • ${clip.speed}x" else "",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 9.sp,
                    color = Color(0xCCFFFFFF)
                  )
                }

                // Left & Right Trim Handles when clip is selected
                if (isSelected) {
                  // Left trim handle
                  Box(
                    modifier = Modifier
                      .align(Alignment.CenterStart)
                      .width(14.dp)
                      .fillMaxHeight()
                      .background(EmeraldPrimary.copy(alpha = 0.7f), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
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

                  // Right trim handle
                  Box(
                    modifier = Modifier
                      .align(Alignment.CenterEnd)
                      .width(14.dp)
                      .fillMaxHeight()
                      .background(EmeraldPrimary.copy(alpha = 0.7f), RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
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

        Spacer(modifier = Modifier.height(3.dp))

        // Track 2: [Fx] Effects Lane (Shows clips' active effects badges)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0x33FFD54F),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
            modifier = Modifier.padding(end = 6.dp)
          ) {
            Text("Fx", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = GoldAccent, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
          }

          project.clips.forEach { clip ->
            val clipWidthDp = msToDp(clip.effectiveDurationMs).coerceAtLeast(48f).dp
            val fxCount = clip.activeEffects.size

            Surface(
              shape = RoundedCornerShape(4.dp),
              color = if (fxCount > 0) Color(0x332A1B3D) else Color(0x11FFFFFF),
              border = BorderStroke(0.5.dp, if (fxCount > 0) GoldAccent.copy(alpha = 0.6f) else Color(0x22FFFFFF)),
              modifier = Modifier
                .width(clipWidthDp)
                .fillMaxHeight()
                .padding(end = 2.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (fxCount > 0) {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(10.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = clip.activeEffects.joinToString(", ") { it.effect.displayName },
                    fontSize = 8.sp,
                    color = GoldAccent,
                    maxLines = 1
                  )
                } else {
                  Text("No effects", fontSize = 8.sp, color = TextMuted)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Track 3: [A] Audio Track (Multi-track with volume, drag to move & long-press)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0x3300E5FF),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
            modifier = Modifier.padding(end = 6.dp)
          ) {
            Text("A", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanAccent, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
          }

          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (project.audioTracks.isEmpty()) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = CharcoalSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxSize()
              ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                  Text("Tap Audio in toolbar to add music or voiceover", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
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
                      detectTapGestures(
                        onTap = { onSelectAudio(audio.id) },
                        onLongPress = { longPressTarget = TimelineTarget.Audio(audio) }
                      )
                    }
                    .pointerInput(audio.id) {
                      detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaMs = (dragAmount.x / dpPerSecond.value * 1000f).toLong()
                        val newStart = (audio.startAtMs + deltaMs).coerceAtLeast(0L)
                        onAudioMove(audio.id, newStart)
                      }
                    }
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
                        modifier = Modifier.size(11.dp)
                      )
                      Spacer(modifier = Modifier.width(3.dp))
                      Text(
                        text = "${audio.title} (${(audio.durationMs / 1000f).toInt()}s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 9.sp,
                        maxLines = 1
                      )
                    }
                    Text(
                      text = if (audio.isMuted) "Mute" else "${(audio.volume * 100).toInt()}%",
                      fontSize = 8.sp,
                      color = EmeraldPrimary
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Track 4: [T] Text & Subtitles Track
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0x33FF2A85),
            border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f)),
            modifier = Modifier.padding(end = 6.dp)
          ) {
            Text("T", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonPink, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
          }

          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            project.textOverlays.forEach { textOverlay ->
              val textOffsetDp = msToDp(textOverlay.startAtMs).dp
              val textWidthDp = msToDp(textOverlay.durationMs).coerceAtLeast(50f).dp
              val isSelected = textOverlay.id == selectedTextId

              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isSelected) CharcoalCardSelected else Color(0xFF2A1B3D),
                border = BorderStroke(1.dp, if (isSelected) NeonPink else Color(0xFF44318D)),
                modifier = Modifier
                  .offset(x = textOffsetDp)
                  .width(textWidthDp)
                  .fillMaxHeight()
                  .pointerInput(textOverlay.id) {
                    detectTapGestures(
                      onTap = { onSelectText(textOverlay.id) },
                      onLongPress = { longPressTarget = TimelineTarget.Text(textOverlay) }
                    )
                  }
                  .pointerInput(textOverlay.id) {
                    detectDragGestures { change, dragAmount ->
                      change.consume()
                      val deltaMs = (dragAmount.x / dpPerSecond.value * 1000f).toLong()
                      val newStart = (textOverlay.startAtMs + deltaMs).coerceAtLeast(0L)
                      onTextMove(textOverlay.id, newStart)
                    }
                  }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.TextFields, contentDescription = null, tint = NeonPink, modifier = Modifier.size(11.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = textOverlay.text,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.White,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Track 5: [S] Stickers Track
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0x33FF9800),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
            modifier = Modifier.padding(end = 6.dp)
          ) {
            Text("S", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldAccent, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
          }

          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            project.stickers.forEach { sticker ->
              val stkOffsetDp = msToDp(sticker.startAtMs).dp
              val stkWidthDp = msToDp(sticker.durationMs).coerceAtLeast(46f).dp
              val isSelected = sticker.id == selectedStickerId

              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isSelected) CharcoalCardSelected else Color(0xFF332005),
                border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color(0xFF66400A)),
                modifier = Modifier
                  .offset(x = stkOffsetDp)
                  .width(stkWidthDp)
                  .fillMaxHeight()
                  .pointerInput(sticker.id) {
                    detectTapGestures(
                      onTap = { onSelectSticker(sticker.id) },
                      onLongPress = { longPressTarget = TimelineTarget.Sticker(sticker) }
                    )
                  }
                  .pointerInput(sticker.id) {
                    detectDragGestures { change, dragAmount ->
                      change.consume()
                      val deltaMs = (dragAmount.x / dpPerSecond.value * 1000f).toLong()
                      val newStart = (sticker.startAtMs + deltaMs).coerceAtLeast(0L)
                      onStickerMove(sticker.id, newStart)
                    }
                  }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = sticker.emojiOrSymbol, fontSize = 11.sp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = sticker.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.White,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }
      }

      // Red Playhead Needle Overlay
      val playheadOffsetDp = (msToDp(playheadMs) + 38f).dp
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

  // 3. Contextual Action Modal triggered on Long-Press of ANY timeline object
  longPressTarget?.let { target ->
    Dialog(onDismissRequest = { longPressTarget = null }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = CharcoalSurface,
        border = BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          when (target) {
            is TimelineTarget.Clip -> {
              Text("Clip: ${target.clip.title}", style = MaterialTheme.typography.titleMedium, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
              Divider(color = CharcoalBorder)

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onSplitClick()
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCut, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Split at Playhead", color = TextPrimary)
              }

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onDuplicateClip(target.clip.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Duplicate Clip", color = TextPrimary)
              }

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onReplaceClipClick(target.clip.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Replace with Gallery Video", color = TextPrimary)
              }

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onDeleteClip(target.clip.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = PlayheadRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Delete Clip", color = PlayheadRed)
              }
            }

            is TimelineTarget.Audio -> {
              Text("Audio: ${target.track.title}", style = MaterialTheme.typography.titleMedium, color = CyanAccent, fontWeight = FontWeight.Bold)
              Divider(color = CharcoalBorder)

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onDuplicateAudio(target.track.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Duplicate Audio Track", color = TextPrimary)
              }

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onDeleteAudio(target.track.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = PlayheadRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Delete Audio Track", color = PlayheadRed)
              }
            }

            is TimelineTarget.Text -> {
              Text("Text: \"${target.overlay.text}\"", style = MaterialTheme.typography.titleMedium, color = NeonPink, fontWeight = FontWeight.Bold)
              Divider(color = CharcoalBorder)

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onEditText(target.overlay.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Edit Typography & Properties", color = TextPrimary)
              }

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onDuplicateText(target.overlay.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Duplicate Text Layer", color = TextPrimary)
              }

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onDeleteText(target.overlay.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = PlayheadRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Delete Text Layer", color = PlayheadRed)
              }
            }

            is TimelineTarget.Sticker -> {
              Text("Sticker: ${target.sticker.emojiOrSymbol} ${target.sticker.label}", style = MaterialTheme.typography.titleMedium, color = GoldAccent, fontWeight = FontWeight.Bold)
              Divider(color = CharcoalBorder)

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onDuplicateSticker(target.sticker.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Duplicate Sticker Layer", color = TextPrimary)
              }

              Row(modifier = Modifier.fillMaxWidth().clickable {
                onDeleteSticker(target.sticker.id)
                longPressTarget = null
              }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = PlayheadRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Delete Sticker Layer", color = PlayheadRed)
              }
            }
          }
        }
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
      .padding(horizontal = 38.dp)
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
