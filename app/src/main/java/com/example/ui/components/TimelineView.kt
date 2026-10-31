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
                // Left trim indicator if selected
                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .align(Alignment.CenterStart)
                      .width(4.dp)
                      .fillMaxHeight()
                      .background(EmeraldPrimary, RoundedCornerShape(2.dp))
                  )
                }

                // Title and duration
                Column(modifier = Modifier.align(Alignment.CenterStart).padding(start = if (isSelected) 6.dp else 2.dp)) {
                  Text(
                    text = clip.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                  )
                  Text(
                    text = "${(clip.effectiveDurationMs / 1000f)}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp
                  )
                }

                // Speed / Filter indicator badge
                if (clip.speed != 1.0f || clip.filter != VideoFilter.ORIGINAL) {
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0x99000000),
                    modifier = Modifier.align(Alignment.TopEnd)
                  ) {
                    Text(
                      text = if (clip.speed != 1.0f) "${clip.speed}x" else "Fx",
                      color = GoldAccent,
                      fontSize = 8.sp,
                      modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                  }
                }

                // Right trim indicator if selected
                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .align(Alignment.CenterEnd)
                      .width(4.dp)
                      .fillMaxHeight()
                      .background(EmeraldPrimary, RoundedCornerShape(2.dp))
                  )
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

        // 3. Audio Track
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
              val audioWidthDp = msToDp(audio.durationMs).coerceAtLeast(50f).dp
              val isSelected = audio.id == selectedAudioId

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) CharcoalCardSelected else CharcoalCard,
                border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color(0xFF1E3A5F)),
                modifier = Modifier
                  .offset(x = audioOffsetDp)
                  .width(audioWidthDp)
                  .fillMaxHeight()
                  .clickable { onSelectAudio(audio.id) }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0D3B66), Color(0xFF00A896))))
                    .padding(horizontal = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.MusicNote, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${audio.title} (${(audio.durationMs / 1000f).toInt()}s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1
                  )
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
