package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
  audioTracks: List<AudioTrack> = emptyList(),
  selectedAudioId: String? = null,
  onTabSelected: (ToolTab) -> Unit,
  onSplitClick: () -> Unit,
  onTrimChanged: (Long, Long) -> Unit,
  onSpeedChanged: (Float) -> Unit,
  onVolumeChanged: (Float, Boolean) -> Unit,
  onFilterChanged: (VideoFilter, Float) -> Unit,
  onEffectChanged: (VideoEffect, Float) -> Unit,
  onTransitionChanged: (TransitionType) -> Unit,
  onTransitionDurationChanged: (Long) -> Unit = {},
  onAddEffect: (VideoEffect, Float) -> Unit = { _, _ -> },
  onRemoveEffect: (String) -> Unit = {},
  onReorderEffects: (Int, Int) -> Unit = { _, _ -> },
  onUpdateEffectIntensity: (String, Float) -> Unit = { _, _ -> },
  onClearEffects: () -> Unit = {},
  onRotateClick: () -> Unit,
  onFlipHClick: () -> Unit,
  onDuplicateClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onReverseClick: () -> Unit,
  onFreezeFrameClick: () -> Unit,
  onReplaceClipClick: () -> Unit = {},
  onSelectAudio: (String) -> Unit = {},
  onAudioVolumeChanged: (String, Float, Boolean) -> Unit = { _, _, _ -> },
  onDeleteAudioClick: (String) -> Unit = {},
  onOpenAudioPicker: () -> Unit,
  onOpenTextEditor: () -> Unit,
  onOpenStickerPicker: () -> Unit,
  onAutoCaptions: () -> Unit,
  onChangeRatio: (AspectRatio) -> Unit,
  onRetouchChanged: (FaceRetouchSettings) -> Unit = {},
  onRetouchReset: () -> Unit = {},
  isRetouchBypass: Boolean = false,
  onRetouchBypassChanged: (Boolean) -> Unit = {},
  effectPresets: List<EffectPreset> = emptyList(),
  onApplyEffectPreset: (EffectPreset) -> Unit = {},
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

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("Original Video Audio", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                      Text(
                        text = if (muted) "Audio muted" else if (vol > 1.0f) "Boosted ${(vol * 100).toInt()}% (+${((vol - 1.0f) * 6).toInt()} dB)" else "Level: ${(vol * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (muted) ErrorRed else if (vol > 1.0f) EmeraldPrimary else TextSecondary
                      )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text("Mute", style = MaterialTheme.typography.labelSmall, color = if (muted) ErrorRed else TextSecondary)
                      Spacer(modifier = Modifier.width(6.dp))
                      Switch(
                        checked = muted,
                        onCheckedChange = {
                          muted = it
                          onVolumeChanged(vol, muted)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ErrorRed, checkedTrackColor = ErrorRed.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("clip_mute_switch")
                      )
                    }
                  }

                  // 0% to 200% Volume Slider
                  Slider(
                    value = vol,
                    onValueChange = {
                      vol = it
                      if (muted && vol > 0.05f) {
                        muted = false
                      }
                      onVolumeChanged(vol, muted)
                    },
                    valueRange = 0f..2f,
                    colors = SliderDefaults.colors(
                      thumbColor = if (vol > 1.0f) EmeraldPrimary else GoldAccent,
                      activeTrackColor = if (vol > 1.0f) EmeraldPrimary else GoldAccent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("clip_volume_slider")
                  )

                  // Volume Quick Presets Row
                  Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    val presets = listOf(
                      Triple("Mute (0%)", 0.0f, true),
                      Triple("50%", 0.5f, false),
                      Triple("100% (1x)", 1.0f, false),
                      Triple("150% (1.5x)", 1.5f, false),
                      Triple("200% (2x Boost)", 2.0f, false)
                    )

                    presets.forEach { (label, value, shouldMute) ->
                      val isSelected = (!muted && kotlin.math.abs(vol - value) < 0.05f) || (muted && shouldMute)
                      FilterChip(
                        selected = isSelected,
                        onClick = {
                          vol = value
                          muted = shouldMute
                          onVolumeChanged(vol, muted)
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                          selectedContainerColor = if (value > 1.0f) EmeraldPrimary else GoldAccent,
                          selectedLabelColor = CharcoalBackground
                        ),
                        modifier = Modifier.testTag("preset_volume_${(value * 100).toInt()}")
                      )
                    }
                  }
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
              var searchQuery by remember { mutableStateOf("") }
              var selectedCategory by remember { mutableStateOf(EffectCategory.ALL) }
              var isGridView by remember { mutableStateOf(true) } // Toggle between Grid & Strip
              val activeEffects = selectedClip?.activeEffects ?: emptyList()
              var selectedActiveEffectId by remember(selectedClip?.id, activeEffects.size) {
                mutableStateOf(activeEffects.firstOrNull()?.id)
              }

              // Selected effect for intensity slider
              val currentFocusedEffect = activeEffects.find { it.id == selectedActiveEffectId } ?: activeEffects.lastOrNull()

              Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Active Effects Stack Panel (if any applied)
                if (activeEffects.isNotEmpty()) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalCard,
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = "Active Stack (${activeEffects.size})",
                          style = MaterialTheme.typography.labelMedium,
                          color = EmeraldPrimary,
                          fontWeight = FontWeight.Bold
                        )
                        TextButton(
                          onClick = {
                            selectedClip?.let { onClearEffects() }
                          },
                          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text("Clear All", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
                        }
                      }

                      // Stack layers row with reorder & delete
                      Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        activeEffects.forEachIndexed { index, applied ->
                          val isFocused = applied.id == currentFocusedEffect?.id
                          Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isFocused) EmeraldPrimary.copy(alpha = 0.2f) else CharcoalSurfaceVariant,
                            border = BorderStroke(1.dp, if (isFocused) EmeraldPrimary else CharcoalBorder),
                            modifier = Modifier
                              .clickable { selectedActiveEffectId = applied.id }
                              .testTag("active_effect_${applied.effect.name}")
                          ) {
                            Row(
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                              Text(
                                text = "${index + 1}. ${applied.effect.displayName} (${(applied.intensity * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isFocused) EmeraldPrimary else TextPrimary,
                                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
                              )
                              // Move Left/Up
                              if (index > 0) {
                                IconButton(
                                  onClick = { onReorderEffects(index, index - 1) },
                                  modifier = Modifier.size(16.dp)
                                ) {
                                  Icon(Icons.Default.ArrowBack, contentDescription = "Move Left", tint = TextMuted, modifier = Modifier.size(12.dp))
                                }
                              }
                              // Move Right/Down
                              if (index < activeEffects.size - 1) {
                                IconButton(
                                  onClick = { onReorderEffects(index, index + 1) },
                                  modifier = Modifier.size(16.dp)
                                ) {
                                  Icon(Icons.Default.ArrowForward, contentDescription = "Move Right", tint = TextMuted, modifier = Modifier.size(12.dp))
                                }
                              }
                              // Remove
                              IconButton(
                                onClick = { onRemoveEffect(applied.id) },
                                modifier = Modifier.size(16.dp)
                              ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Effect", tint = ErrorRed, modifier = Modifier.size(12.dp))
                              }
                            }
                          }
                        }
                      }

                      // Focused Effect Intensity Slider
                      if (currentFocusedEffect != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                          Text(
                            text = "${currentFocusedEffect.effect.displayName} Intensity",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                          )
                          Text(
                            text = "${(currentFocusedEffect.intensity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                          )
                        }
                        Slider(
                          value = currentFocusedEffect.intensity,
                          onValueChange = { newIntensity ->
                            onUpdateEffectIntensity(currentFocusedEffect.id, newIntensity)
                          },
                          valueRange = 0f..1f,
                          colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary),
                          modifier = Modifier.testTag("effect_intensity_slider")
                        )
                      }
                    }
                  }
                }

                // 2. Search, Layout Toggle (Grid vs Horizontal Strip) & Category Filters
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 50+ Effects...", style = MaterialTheme.typography.labelMedium, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                      if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                          Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                      }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = EmeraldPrimary,
                      unfocusedBorderColor = CharcoalBorder,
                      focusedTextColor = TextPrimary,
                      unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("effects_search_field")
                  )

                  // Layout Mode Toggle Button (Grid [⊞] vs Strip [↔])
                  IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier
                      .size(46.dp)
                      .background(if (isGridView) EmeraldPrimary.copy(alpha = 0.2f) else CharcoalCard, RoundedCornerShape(8.dp))
                      .border(BorderStroke(1.dp, if (isGridView) EmeraldPrimary else CharcoalBorder), RoundedCornerShape(8.dp))
                      .testTag("effects_layout_toggle")
                  ) {
                    Icon(
                      imageVector = if (isGridView) Icons.Default.GridView else Icons.Default.ViewStream,
                      contentDescription = if (isGridView) "Switch to Strip" else "Switch to Grid",
                      tint = if (isGridView) EmeraldPrimary else TextPrimary,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category Chips Row
                Row(
                  modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  EffectCategory.values().forEach { cat ->
                    FilterChip(
                      selected = selectedCategory == cat,
                      onClick = { selectedCategory = cat },
                      label = { Text(cat.displayName) },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = CharcoalBackground
                      ),
                      modifier = Modifier.testTag("category_chip_${cat.name}")
                    )
                  }
                }

                // 2.5 Owner Studio Presets (Combinations of multiple effects with calibrated parameters)
                val publishedPresets = remember(effectPresets) { effectPresets.filter { it.isPublished } }
                if (publishedPresets.isNotEmpty() && searchQuery.isBlank() && (selectedCategory == EffectCategory.ALL || selectedCategory == EffectCategory.CINEMATIC)) {
                  Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑 Owner Studio Presets", style = MaterialTheme.typography.labelMedium, color = GoldAccent, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0x33FFD54F)) {
                          Text("PRO COMBO", fontSize = 8.sp, color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                      }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      publishedPresets.forEach { preset ->
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = CharcoalCard,
                          border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                          modifier = Modifier
                            .width(170.dp)
                            .clickable { onApplyEffectPreset(preset) }
                            .testTag("apply_preset_${preset.id}")
                        ) {
                          Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                              Text(preset.thumbnailIcon, fontSize = 16.sp)
                              Spacer(modifier = Modifier.width(4.dp))
                              Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                              )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                              text = "${preset.effects.size} Effects: " + preset.effects.joinToString("+") { it.effect.displayName.take(7) },
                              fontSize = 8.sp,
                              color = TextMuted,
                              maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                              shape = RoundedCornerShape(4.dp),
                              color = GoldAccent.copy(alpha = 0.2f),
                              modifier = Modifier.fillMaxWidth()
                            ) {
                              Text(
                                text = "Apply Combo ✨",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 3.dp)
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }

                // 3. Effects Library Grid / Strip with Thumbnails
                val filteredEffects = remember(searchQuery, selectedCategory) {
                  VideoEffect.values().filter { eff ->
                    eff != VideoEffect.NONE &&
                      (selectedCategory == EffectCategory.ALL || eff.category == selectedCategory) &&
                      (searchQuery.isBlank() || eff.displayName.contains(searchQuery, ignoreCase = true) || eff.description.contains(searchQuery, ignoreCase = true))
                  }
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Library (${filteredEffects.size} Effects • ${if (isGridView) "Grid View" else "Strip View"})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (isGridView) {
                  // Responsive 3-column / 2-column Grid of Effects
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .heightIn(max = 240.dp)
                      .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    filteredEffects.chunked(3).forEach { rowEffects ->
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        rowEffects.forEach { effect ->
                          val isApplied = activeEffects.any { it.effect == effect }
                          EffectGridThumbnailCard(
                            effect = effect,
                            isApplied = isApplied,
                            modifier = Modifier.weight(1f),
                            onClick = {
                              if (isApplied) {
                                val existingId = activeEffects.find { it.effect == effect }?.id
                                if (existingId != null) selectedActiveEffectId = existingId
                              } else {
                                onAddEffect(effect, 0.8f)
                              }
                            }
                          )
                        }
                        // Spacer fillers for incomplete rows
                        if (rowEffects.size < 3) {
                          repeat(3 - rowEffects.size) {
                            Spacer(modifier = Modifier.weight(1f))
                          }
                        }
                      }
                    }
                  }
                } else {
                  // Horizontal Browsing Strip
                  Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    filteredEffects.forEach { effect ->
                      val isApplied = activeEffects.any { it.effect == effect }
                      EffectStripThumbnailCard(
                        effect = effect,
                        isApplied = isApplied,
                        onClick = {
                          if (isApplied) {
                            val existingId = activeEffects.find { it.effect == effect }?.id
                            if (existingId != null) selectedActiveEffectId = existingId
                          } else {
                            onAddEffect(effect, 0.8f)
                          }
                        }
                      )
                    }
                  }
                }
              }
            }

            ToolTab.RETOUCH -> {
              val currentRetouch = selectedClip?.retouch ?: FaceRetouchSettings()
              var enabled by remember(selectedClip?.id, currentRetouch.isEnabled) { mutableStateOf(currentRetouch.isEnabled) }
              var overallIntensity by remember(selectedClip?.id, currentRetouch.overallIntensity) { mutableFloatStateOf(currentRetouch.overallIntensity) }
              var skinSmoothing by remember(selectedClip?.id, currentRetouch.skinSmoothing) { mutableFloatStateOf(currentRetouch.skinSmoothing) }
              var faceBrightness by remember(selectedClip?.id, currentRetouch.faceBrightness) { mutableFloatStateOf(currentRetouch.faceBrightness) }
              var blemishReduction by remember(selectedClip?.id, currentRetouch.blemishReduction) { mutableFloatStateOf(currentRetouch.blemishReduction) }
              var eyeEnhancement by remember(selectedClip?.id, currentRetouch.eyeEnhancement) { mutableFloatStateOf(currentRetouch.eyeEnhancement) }
              var naturalSkinTone by remember(selectedClip?.id, currentRetouch.naturalSkinTone) { mutableFloatStateOf(currentRetouch.naturalSkinTone) }

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Header: Master Enable Switch & Before/After Comparison
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Retouch", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                      checked = enabled,
                      onCheckedChange = {
                        enabled = it
                        onRetouchChanged(
                          currentRetouch.copy(
                            isEnabled = enabled,
                            overallIntensity = overallIntensity,
                            skinSmoothing = skinSmoothing,
                            faceBrightness = faceBrightness,
                            blemishReduction = blemishReduction,
                            eyeEnhancement = eyeEnhancement,
                            naturalSkinTone = naturalSkinTone
                          )
                        )
                      },
                      colors = SwitchDefaults.colors(checkedThumbColor = CoralPink, checkedTrackColor = CoralPink.copy(alpha = 0.4f)),
                      modifier = Modifier.testTag("retouch_enable_switch")
                    )
                  }

                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Before/After comparison button
                    FilledTonalButton(
                      onClick = { onRetouchBypassChanged(!isRetouchBypass) },
                      colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isRetouchBypass) CoralPink else CharcoalCard,
                        contentColor = if (isRetouchBypass) CharcoalBackground else TextPrimary
                      ),
                      shape = RoundedCornerShape(8.dp),
                      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                      modifier = Modifier.testTag("retouch_before_after_button")
                    ) {
                      Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(if (isRetouchBypass) "Showing Before" else "Hold Compare", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Reset button
                    TextButton(
                      onClick = {
                        enabled = false
                        overallIntensity = 0.7f
                        skinSmoothing = 0.65f
                        faceBrightness = 0.4f
                        blemishReduction = 0.6f
                        eyeEnhancement = 0.5f
                        naturalSkinTone = 0.5f
                        onRetouchReset()
                      },
                      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                      modifier = Modifier.testTag("retouch_reset_button")
                    ) {
                      Text("Reset", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
                    }
                  }
                }

                // Overall Intensity Slider (0% - 100%)
                Card(
                  colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                  shape = RoundedCornerShape(8.dp),
                  border = BorderStroke(1.dp, if (enabled) CoralPink.copy(alpha = 0.4f) else CharcoalBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                      Text("Overall Retouch Intensity", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                      Text("${(overallIntensity * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = CoralPink, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                      value = overallIntensity,
                      onValueChange = {
                        overallIntensity = it
                        if (!enabled) enabled = true
                        onRetouchChanged(
                          currentRetouch.copy(
                            isEnabled = true,
                            overallIntensity = overallIntensity,
                            skinSmoothing = skinSmoothing,
                            faceBrightness = faceBrightness,
                            blemishReduction = blemishReduction,
                            eyeEnhancement = eyeEnhancement,
                            naturalSkinTone = naturalSkinTone
                          )
                        )
                      },
                      valueRange = 0f..1f,
                      colors = SliderDefaults.colors(thumbColor = CoralPink, activeTrackColor = CoralPink),
                      modifier = Modifier.testTag("retouch_overall_intensity_slider")
                    )
                  }
                }

                // Specific Retouch Parameter Sliders
                RetouchSliderItem(
                  label = "Skin Smoothing",
                  description = "Bilateral edge-preserving pore and texture smoothing",
                  value = skinSmoothing,
                  color = CoralPink,
                  testTag = "retouch_skin_smoothing_slider",
                  onValueChange = {
                    skinSmoothing = it
                    if (!enabled) enabled = true
                    onRetouchChanged(
                      currentRetouch.copy(
                        isEnabled = true,
                        skinSmoothing = skinSmoothing
                      )
                    )
                  }
                )

                RetouchSliderItem(
                  label = "Face Brightness",
                  description = "Targeted facial gamma lift and under-eye shadow reduction",
                  value = faceBrightness,
                  color = GoldAccent,
                  testTag = "retouch_face_brightness_slider",
                  onValueChange = {
                    faceBrightness = it
                    if (!enabled) enabled = true
                    onRetouchChanged(
                      currentRetouch.copy(
                        isEnabled = true,
                        faceBrightness = faceBrightness
                      )
                    )
                  }
                )

                RetouchSliderItem(
                  label = "Blemish Reduction",
                  description = "Localized median blending for spot and redness softening",
                  value = blemishReduction,
                  color = EmeraldPrimary,
                  testTag = "retouch_blemish_reduction_slider",
                  onValueChange = {
                    blemishReduction = it
                    if (!enabled) enabled = true
                    onRetouchChanged(
                      currentRetouch.copy(
                        isEnabled = true,
                        blemishReduction = blemishReduction
                      )
                    )
                  }
                )

                RetouchSliderItem(
                  label = "Eye Enhancement",
                  description = "Sclera whitening, iris clarity and catchlight boost",
                  value = eyeEnhancement,
                  color = Color(0xFF00E5FF),
                  testTag = "retouch_eye_enhancement_slider",
                  onValueChange = {
                    eyeEnhancement = it
                    if (!enabled) enabled = true
                    onRetouchChanged(
                      currentRetouch.copy(
                        isEnabled = true,
                        eyeEnhancement = eyeEnhancement
                      )
                    )
                  }
                )

                RetouchSliderItem(
                  label = "Natural Skin Tone",
                  description = "Authentic warm undertone balancing and vitality",
                  value = naturalSkinTone,
                  color = Color(0xFFFF9100),
                  testTag = "retouch_skin_tone_slider",
                  onValueChange = {
                    naturalSkinTone = it
                    if (!enabled) enabled = true
                    onRetouchChanged(
                      currentRetouch.copy(
                        isEnabled = true,
                        naturalSkinTone = naturalSkinTone
                      )
                    )
                  }
                )
              }
            }

            ToolTab.ADJUST -> {
              var brightness by remember(selectedClip?.id) { mutableFloatStateOf(1f) }
              var contrast by remember(selectedClip?.id) { mutableFloatStateOf(1f) }
              var saturation by remember(selectedClip?.id) { mutableFloatStateOf(1f) }

              Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                RetouchSliderItem(
                  label = "Brightness",
                  description = "Overall frame luminance level",
                  value = (brightness / 2f).coerceIn(0f, 1f),
                  color = GoldAccent,
                  testTag = "adjust_brightness_slider",
                  onValueChange = { brightness = it * 2f }
                )
                RetouchSliderItem(
                  label = "Contrast",
                  description = "Dynamic tonal range between darks and highlights",
                  value = (contrast / 2f).coerceIn(0f, 1f),
                  color = EmeraldPrimary,
                  testTag = "adjust_contrast_slider",
                  onValueChange = { contrast = it * 2f }
                )
                RetouchSliderItem(
                  label = "Saturation",
                  description = "Chroma and color vividness",
                  value = (saturation / 2f).coerceIn(0f, 1f),
                  color = Color(0xFF00E5FF),
                  testTag = "adjust_saturation_slider",
                  onValueChange = { saturation = it * 2f }
                )
              }
            }

            ToolTab.TRANSITIONS -> {
              val currentTrans = selectedClip?.transitionToNext ?: TransitionType.NONE
              var durationMs by remember(selectedClip?.id, selectedClip?.transitionDurationMs) {
                mutableLongStateOf(selectedClip?.transitionDurationMs ?: 500L)
              }

              Column(modifier = Modifier.fillMaxWidth()) {
                // Header with current active transition and Duration slider (0.2s to 3.0s)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Active Transition: ${currentTrans.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (currentTrans != TransitionType.NONE) EmeraldPrimary else TextSecondary,
                    fontWeight = FontWeight.SemiBold
                  )
                  if (currentTrans != TransitionType.NONE) {
                    TextButton(
                      onClick = { onTransitionChanged(TransitionType.NONE) },
                      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text("Remove / None", style = MaterialTheme.typography.labelSmall, color = ErrorRed)
                    }
                  }
                }

                if (currentTrans != TransitionType.NONE) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Transition Duration", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                      text = "${(durationMs / 1000f).formatOneDecimal()}s",
                      style = MaterialTheme.typography.labelSmall,
                      color = EmeraldPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  Slider(
                    value = durationMs.toFloat(),
                    onValueChange = {
                      durationMs = it.toLong()
                      onTransitionDurationChanged(durationMs)
                    },
                    valueRange = 200f..3000f,
                    steps = 27, // 0.1s increments between 0.2s and 3.0s
                    colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary),
                    modifier = Modifier.testTag("transition_duration_slider")
                  )

                  // Quick duration presets
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    listOf(300L, 500L, 800L, 1000L, 1500L, 2000L).forEach { presetMs ->
                      AssistChip(
                        onClick = {
                          durationMs = presetMs
                          onTransitionDurationChanged(presetMs)
                        },
                        label = { Text("${(presetMs / 1000f).formatOneDecimal()}s", fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                          labelColor = if (durationMs == presetMs) EmeraldPrimary else TextSecondary
                        )
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Choose from 20 Transitions (${TransitionType.primaryTransitions.size} Total)",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted,
                  modifier = Modifier.padding(bottom = 4.dp)
                )

                // 20 Transitions Chips / Cards Row
                Row(
                  modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  TransitionType.primaryTransitions.forEach { t ->
                    val isSelected = (currentTrans == t)
                    FilterChip(
                      selected = isSelected,
                      onClick = { onTransitionChanged(t) },
                      label = { Text(t.displayName) },
                      leadingIcon = {
                        Icon(
                          imageVector = if (t == TransitionType.NONE) Icons.Default.Block else Icons.Default.Transform,
                          contentDescription = null,
                          modifier = Modifier.size(14.dp)
                        )
                      },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = CharcoalBackground
                      ),
                      modifier = Modifier.testTag("transition_chip_${t.name}")
                    )
                  }
                }
              }
            }

            ToolTab.TRANSFORM -> {
              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = onReplaceClipClick,
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary)
                ) {
                  Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Replace")
                }
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
              Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Text("Add Music / SFX / Voice")
                  }
                }

                if (audioTracks.isNotEmpty()) {
                  Text("Timeline Audio Tracks (${audioTracks.size})", style = MaterialTheme.typography.labelSmall, color = TextMuted)

                  val currentTrack = audioTracks.find { it.id == selectedAudioId } ?: audioTracks.first()
                  var vol by remember(currentTrack.id) { mutableFloatStateOf(currentTrack.volume) }
                  var muted by remember(currentTrack.id) { mutableStateOf(currentTrack.isMuted) }

                  // Track selector chips
                  Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    audioTracks.forEach { track ->
                      val isSelected = track.id == currentTrack.id
                      FilterChip(
                        selected = isSelected,
                        onClick = { onSelectAudio(track.id) },
                        label = { Text("${track.title} (${track.category})") },
                        colors = FilterChipDefaults.filterChipColors(
                          selectedContainerColor = EmeraldPrimary,
                          selectedLabelColor = CharcoalBackground
                        )
                      )
                    }
                  }

                  // Active Track Controls
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalCard,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(currentTrack.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text("Mute", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                          Spacer(modifier = Modifier.width(4.dp))
                          Switch(
                            checked = muted,
                            onCheckedChange = {
                              muted = it
                              onAudioVolumeChanged(currentTrack.id, vol, muted)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                          )
                          Spacer(modifier = Modifier.width(8.dp))
                          IconButton(
                            onClick = { onDeleteAudioClick(currentTrack.id) },
                            modifier = Modifier.size(24.dp)
                          ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Audio", tint = ErrorRed, modifier = Modifier.size(16.dp))
                          }
                        }
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text("Volume", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("${(vol * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                      }
                      Slider(
                        value = vol,
                        onValueChange = {
                          vol = it
                          onAudioVolumeChanged(currentTrack.id, vol, muted)
                        },
                        valueRange = 0f..2f,
                        colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                      )
                    }
                  }
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
      ToolIconItem(icon = Icons.Default.Face, label = "Retouch", isSelected = activeTab == ToolTab.RETOUCH) { onTabSelected(ToolTab.RETOUCH) }
      ToolIconItem(icon = Icons.Default.Transform, label = "Transitions", isSelected = activeTab == ToolTab.TRANSITIONS) { onTabSelected(ToolTab.TRANSITIONS) }
      ToolIconItem(icon = Icons.Default.Tune, label = "Adjust", isSelected = activeTab == ToolTab.ADJUST) { onTabSelected(ToolTab.ADJUST) }
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

@Composable
private fun EffectGridThumbnailCard(
  effect: VideoEffect,
  isApplied: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isApplied) EmeraldDark.copy(alpha = 0.35f) else CharcoalCard,
    border = BorderStroke(1.dp, if (isApplied) EmeraldPrimary else CharcoalBorder),
    modifier = modifier
      .clickable { onClick() }
      .testTag("effect_card_${effect.name}")
  ) {
    Column(modifier = Modifier.padding(6.dp)) {
      // Visual Effect Thumbnail
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp)
          .background(CharcoalSurfaceVariant, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = getEffectIcon(effect),
          contentDescription = null,
          tint = if (isApplied) EmeraldPrimary else GoldAccent,
          modifier = Modifier.size(22.dp)
        )
        if (effect.isPro) {
          Surface(
            shape = RoundedCornerShape(bottomStart = 4.dp),
            color = GoldAccent.copy(alpha = 0.85f),
            modifier = Modifier.align(Alignment.TopEnd)
          ) {
            Text(
              text = "PRO",
              color = CharcoalBackground,
              fontSize = 7.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = effect.displayName,
        style = MaterialTheme.typography.labelSmall,
        color = if (isApplied) EmeraldPrimary else TextPrimary,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        fontSize = 10.sp
      )

      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isApplied) "Applied" else "+ Add",
          style = MaterialTheme.typography.labelSmall,
          color = if (isApplied) EmeraldPrimary else TextMuted,
          fontSize = 8.sp,
          fontWeight = if (isApplied) FontWeight.Bold else FontWeight.Normal
        )
        Icon(
          imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
          contentDescription = null,
          tint = if (isApplied) EmeraldPrimary else TextMuted,
          modifier = Modifier.size(10.dp)
        )
      }
    }
  }
}

@Composable
private fun EffectStripThumbnailCard(
  effect: VideoEffect,
  isApplied: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isApplied) EmeraldDark.copy(alpha = 0.35f) else CharcoalCard,
    border = BorderStroke(1.dp, if (isApplied) EmeraldPrimary else CharcoalBorder),
    modifier = Modifier
      .width(130.dp)
      .clickable { onClick() }
      .testTag("effect_card_${effect.name}")
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = getEffectIcon(effect),
          contentDescription = null,
          tint = if (isApplied) EmeraldPrimary else GoldAccent,
          modifier = Modifier.size(16.dp)
        )
        if (effect.isPro) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = GoldAccent.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, GoldAccent)
          ) {
            Text(
              text = "PRO",
              color = GoldAccent,
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = effect.displayName,
        style = MaterialTheme.typography.labelMedium,
        color = if (isApplied) EmeraldPrimary else TextPrimary,
        fontWeight = FontWeight.Bold,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = effect.description,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        fontSize = 9.sp,
        maxLines = 2,
        lineHeight = 11.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
          contentDescription = null,
          tint = if (isApplied) EmeraldPrimary else TextSecondary,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
          text = if (isApplied) "Applied" else "Add Effect",
          style = MaterialTheme.typography.labelSmall,
          color = if (isApplied) EmeraldPrimary else TextSecondary,
          fontSize = 9.sp
        )
      }
    }
  }
}

@Composable
private fun RetouchSliderItem(
  label: String,
  description: String,
  value: Float,
  color: Color,
  testTag: String,
  onValueChange: (Float) -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = CharcoalCard),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, CharcoalBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(label, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
          Text(description, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        }
        Text(
          text = "${(value * 100).toInt()}%",
          style = MaterialTheme.typography.labelMedium,
          color = color,
          fontWeight = FontWeight.Bold
        )
      }
      Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color),
        modifier = Modifier.testTag(testTag)
      )
    }
  }
}

private fun getEffectIcon(effect: VideoEffect): ImageVector {
  return when (effect.category) {
    EffectCategory.CINEMATIC -> Icons.Default.MovieFilter
    EffectCategory.GLITCH -> Icons.Default.BrokenImage
    EffectCategory.LIGHT -> Icons.Default.WbIncandescent
    EffectCategory.RETRO -> Icons.Default.CameraRoll
    EffectCategory.BLUR -> Icons.Default.BlurOn
    EffectCategory.CREATIVE -> Icons.Default.Palette
    EffectCategory.COLOR -> Icons.Default.ColorLens
    EffectCategory.BODY -> Icons.Default.DirectionsRun
    EffectCategory.SHAKE -> Icons.Default.Vibration
    EffectCategory.PARTICLES -> Icons.Default.Grain
    EffectCategory.DISTORTION -> Icons.Default.Waves
    EffectCategory.ALL -> Icons.Default.AutoAwesome
  }
}

private fun getTabTitle(tab: ToolTab): String {
  return when (tab) {
    ToolTab.SPLIT -> "Split Clip"
    ToolTab.TRIM -> "Trim Range"
    ToolTab.SPEED -> "Speed Control"
    ToolTab.VOLUME -> "Volume & Fade"
    ToolTab.FILTERS -> "Color Grading Filters"
    ToolTab.EFFECTS -> "Visual Effects Library"
    ToolTab.TRANSITIONS -> "Clip Transition"
    ToolTab.RETOUCH -> "Professional Face Retouch"
    ToolTab.ADJUST -> "Color & Tone Adjust"
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

