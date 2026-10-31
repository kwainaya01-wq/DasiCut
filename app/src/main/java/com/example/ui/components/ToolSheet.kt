package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ToolTab

@Composable
fun ToolSheet(
  activeTab: ToolTab,
  selectedClip: VideoClip?,
  onTabSelected: (ToolTab) -> Unit,
  onSplitClick: () -> Unit,
  onTrimChanged: (Long, Long) -> Unit,
  onSpeedChanged: (Float) -> Unit,
  onVolumeChanged: (Float, Boolean) -> Unit,
  onFilterChanged: (VideoFilter, Float) -> Unit,
  onEffectChanged: (VideoEffect, Float) -> Unit,
  onTransitionChanged: (TransitionType) -> Unit,
  onRotateClick: () -> Unit,
  onFlipHClick: () -> Unit,
  onDuplicateClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onReverseClick: () -> Unit,
  onFreezeFrameClick: () -> Unit,
  onOpenAudioPicker: () -> Unit,
  onOpenTextEditor: () -> Unit,
  onOpenStickerPicker: () -> Unit,
  onAutoCaptions: () -> Unit,
  onChangeRatio: (AspectRatio) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(CharcoalBackground)
      .border(BorderStroke(1.dp, CharcoalBorder))
  ) {
    // 1. Contextual Sub-Tool Control Drawer (Expands when a tool tab is active)
    if (activeTab != ToolTab.NONE) {
      Surface(
        color = CharcoalSurface,
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CharcoalBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Drawer Header with Close X
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = getTabTitle(activeTab),
              style = MaterialTheme.typography.titleMedium,
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
            IconButton(
              onClick = { onTabSelected(ToolTab.NONE) },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Drawer content per active tool
          when (activeTab) {
            ToolTab.SPLIT -> {
              Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                  "Splits the selected clip at the current playhead position into two editable clips.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                  onClick = onSplitClick,
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth().testTag("split_confirm_button")
                ) {
                  Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Split Clip Here", fontWeight = FontWeight.Bold)
                }
              }
            }

            ToolTab.TRIM -> {
              if (selectedClip != null) {
                var startVal by remember(selectedClip.id) { mutableFloatStateOf(selectedClip.trimStartMs.toFloat()) }
                var endVal by remember(selectedClip.id) { mutableFloatStateOf(selectedClip.trimEndMs.toFloat()) }
                val maxDuration = selectedClip.durationMs.toFloat()

                Column {
                  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("In: ${(startVal / 1000f).formatOneDecimal()}s", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                    Text("Out: ${(endVal / 1000f).formatOneDecimal()}s", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                  }
                  RangeSlider(
                    value = startVal..endVal,
                    onValueChange = { range ->
                      if (range.endInclusive - range.start >= 200f) {
                        startVal = range.start
                        endVal = range.endInclusive
                        onTrimChanged(startVal.toLong(), endVal.toLong())
                      }
                    },
                    valueRange = 0f..maxDuration,
                    colors = SliderDefaults.colors(
                      thumbColor = EmeraldPrimary,
                      activeTrackColor = EmeraldPrimary,
                      inactiveTrackColor = CharcoalCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }
            }

            ToolTab.SPEED -> {
              if (selectedClip != null) {
                var speed by remember(selectedClip.id) { mutableFloatStateOf(selectedClip.speed) }
                val speeds = listOf(0.2f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 5.0f)

                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    speeds.forEach { s ->
                      FilterChip(
                        selected = (speed == s),
                        onClick = {
                          speed = s
                          onSpeedChanged(s)
                        },
                        label = { Text("${s}x") },
                        colors = FilterChipDefaults.filterChipColors(
                          selectedContainerColor = EmeraldPrimary,
                          selectedLabelColor = CharcoalBackground
                        )
                      )
                    }
                  }
                }
              }
            }

            ToolTab.VOLUME -> {
              if (selectedClip != null) {
                var vol by remember(selectedClip.id) { mutableFloatStateOf(selectedClip.volume) }
                var muted by remember(selectedClip.id) { mutableStateOf(selectedClip.isMuted) }

                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Volume: ${(vol * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text("Mute", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                      Spacer(modifier = Modifier.width(6.dp))
                      Switch(
                        checked = muted,
                        onCheckedChange = {
                          muted = it
                          onVolumeChanged(vol, muted)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                      )
                    }
                  }
                  Slider(
                    value = vol,
                    onValueChange = {
                      vol = it
                      onVolumeChanged(vol, muted)
                    },
                    valueRange = 0f..2f,
                    colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                  )
                }
              }
            }

            ToolTab.FILTERS -> {
              var currentFilter by remember(selectedClip?.id) { mutableStateOf(selectedClip?.filter ?: VideoFilter.ORIGINAL) }
              var intensity by remember(selectedClip?.id) { mutableFloatStateOf(selectedClip?.filterIntensity ?: 1f) }

              Column {
                Row(
                  modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  VideoFilter.values().forEach { filter ->
                    FilterChip(
                      selected = (currentFilter == filter),
                      onClick = {
                        currentFilter = filter
                        onFilterChanged(filter, intensity)
                      },
                      label = { Text(filter.displayName) },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldAccent,
                        selectedLabelColor = CharcoalBackground
                      )
                    )
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Intensity", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                  Text("${(intensity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                }
                Slider(
                  value = intensity,
                  onValueChange = {
                    intensity = it
                    onFilterChanged(currentFilter, intensity)
                  },
                  valueRange = 0f..1f,
                  colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                )
              }
            }

            ToolTab.EFFECTS -> {
              var currentEffect by remember(selectedClip?.id) { mutableStateOf(selectedClip?.effect ?: VideoEffect.NONE) }
              var intensity by remember(selectedClip?.id) { mutableFloatStateOf(selectedClip?.effectIntensity ?: 0.8f) }

              Column {
                Row(
                  modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  VideoEffect.values().forEach { effect ->
                    FilterChip(
                      selected = (currentEffect == effect),
                      onClick = {
                        currentEffect = effect
                        onEffectChanged(effect, intensity)
                      },
                      label = { Text(effect.displayName) },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = CharcoalBackground
                      )
                    )
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Effect Intensity", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                  Text("${(intensity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                }
                Slider(
                  value = intensity,
                  onValueChange = {
                    intensity = it
                    onEffectChanged(currentEffect, intensity)
                  },
                  valueRange = 0f..1f,
                  colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                )
              }
            }

            ToolTab.TRANSITIONS -> {
              val currentTrans = selectedClip?.transitionToNext ?: TransitionType.NONE
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                TransitionType.values().forEach { t ->
                  FilterChip(
                    selected = (currentTrans == t),
                    onClick = { onTransitionChanged(t) },
                    label = { Text(t.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = EmeraldPrimary,
                      selectedLabelColor = CharcoalBackground
                    )
                  )
                }
              }
            }

            ToolTab.TRANSFORM -> {
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(onClick = onRotateClick, shape = RoundedCornerShape(8.dp)) {
                  Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Rotate 90°")
                }
                OutlinedButton(onClick = onFlipHClick, shape = RoundedCornerShape(8.dp)) {
                  Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Flip")
                }
                OutlinedButton(onClick = onDuplicateClick, shape = RoundedCornerShape(8.dp)) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Duplicate")
                }
                OutlinedButton(onClick = onReverseClick, shape = RoundedCornerShape(8.dp)) {
                  Icon(Icons.Default.FastRewind, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Reverse")
                }
                OutlinedButton(onClick = onFreezeFrameClick, shape = RoundedCornerShape(8.dp)) {
                  Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Freeze 2s")
                }
                OutlinedButton(
                  onClick = onDeleteClick,
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                  Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Delete")
                }
              }
            }

            ToolTab.CANVAS -> {
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                AspectRatio.values().forEach { ratio ->
                  Button(
                    onClick = { onChangeRatio(ratio) },
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("${ratio.label} (${ratio.subtitle})")
                  }
                }
              }
            }

            ToolTab.AUDIO -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = onOpenAudioPicker,
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add Music / SFX")
                }
              }
            }

            ToolTab.TEXT -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = onOpenTextEditor,
                  colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = Color.White),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("+ Add Text")
                }
                Button(
                  onClick = onAutoCaptions,
                  colors = ButtonDefaults.buttonColors(containerColor = CharcoalCard, contentColor = GoldAccent),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.Subtitles, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("AI Auto Captions")
                }
              }
            }

            ToolTab.STICKERS -> {
              Button(
                onClick = onOpenStickerPicker,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = CharcoalBackground),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.EmojiEmotions, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Choose Indian Stickers & Emojis")
              }
            }

            ToolTab.CAPTIONS -> {
              Button(
                onClick = onAutoCaptions,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate AI Hinglish Captions")
              }
            }

            ToolTab.NONE -> {}
          }
        }
      }
    }

    // 2. Main Tool Navigation Row (Horizontally scrollable icons row)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Spacer(modifier = Modifier.width(8.dp))

      ToolIconItem(icon = Icons.Default.ContentCut, label = "Split", isSelected = activeTab == ToolTab.SPLIT) { onTabSelected(ToolTab.SPLIT) }
      ToolIconItem(icon = Icons.Default.Tune, label = "Trim", isSelected = activeTab == ToolTab.TRIM) { onTabSelected(ToolTab.TRIM) }
      ToolIconItem(icon = Icons.Default.Speed, label = "Speed", isSelected = activeTab == ToolTab.SPEED) { onTabSelected(ToolTab.SPEED) }
      ToolIconItem(icon = Icons.Default.VolumeUp, label = "Volume", isSelected = activeTab == ToolTab.VOLUME) { onTabSelected(ToolTab.VOLUME) }
      ToolIconItem(icon = Icons.Default.FilterVintage, label = "Filters", isSelected = activeTab == ToolTab.FILTERS) { onTabSelected(ToolTab.FILTERS) }
      ToolIconItem(icon = Icons.Default.AutoAwesome, label = "Effects", isSelected = activeTab == ToolTab.EFFECTS) { onTabSelected(ToolTab.EFFECTS) }
      ToolIconItem(icon = Icons.Default.Transform, label = "Transitions", isSelected = activeTab == ToolTab.TRANSITIONS) { onTabSelected(ToolTab.TRANSITIONS) }
      ToolIconItem(icon = Icons.Default.CropRotate, label = "Transform", isSelected = activeTab == ToolTab.TRANSFORM) { onTabSelected(ToolTab.TRANSFORM) }
      ToolIconItem(icon = Icons.Default.MusicNote, label = "Audio", isSelected = activeTab == ToolTab.AUDIO) { onTabSelected(ToolTab.AUDIO) }
      ToolIconItem(icon = Icons.Default.TextFields, label = "Text", isSelected = activeTab == ToolTab.TEXT) { onTabSelected(ToolTab.TEXT) }
      ToolIconItem(icon = Icons.Default.EmojiEmotions, label = "Stickers", isSelected = activeTab == ToolTab.STICKERS) { onTabSelected(ToolTab.STICKERS) }
      ToolIconItem(icon = Icons.Default.Subtitles, label = "Captions", isSelected = activeTab == ToolTab.CAPTIONS) { onTabSelected(ToolTab.CAPTIONS) }
      ToolIconItem(icon = Icons.Default.AspectRatio, label = "Canvas", isSelected = activeTab == ToolTab.CANVAS) { onTabSelected(ToolTab.CANVAS) }

      Spacer(modifier = Modifier.width(8.dp))
    }
  }
}

@Composable
private fun ToolIconItem(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable { onClick() }
      .padding(horizontal = 4.dp)
      .testTag("tool_item_$label")
  ) {
    Surface(
      shape = CircleShape,
      color = if (isSelected) EmeraldPrimary else CharcoalCard,
      modifier = Modifier.size(42.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = if (isSelected) CharcoalBackground else TextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (isSelected) EmeraldPrimary else TextSecondary,
      fontSize = 11.sp
    )
  }
}

private fun getTabTitle(tab: ToolTab): String {
  return when (tab) {
    ToolTab.SPLIT -> "Split Clip"
    ToolTab.TRIM -> "Trim Range"
    ToolTab.SPEED -> "Speed Control"
    ToolTab.VOLUME -> "Volume & Fade"
    ToolTab.FILTERS -> "Color Grading Filters"
    ToolTab.EFFECTS -> "Visual Effects"
    ToolTab.TRANSITIONS -> "Clip Transition"
    ToolTab.TRANSFORM -> "Transform & Modify"
    ToolTab.AUDIO -> "Audio & Sound FX"
    ToolTab.TEXT -> "Text & Typography"
    ToolTab.STICKERS -> "Stickers & Emojis"
    ToolTab.CAPTIONS -> "Auto Captions"
    ToolTab.CANVAS -> "Aspect Ratio"
    ToolTab.NONE -> ""
  }
}

private fun Float.formatOneDecimal(): String {
  return "%.1f".format(this)
}
