package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.media.VideoEffectsAndTransitionsRenderer
import com.example.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.EditorViewModel
import java.util.UUID

enum class OwnerStudioTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  EFFECTS("Effects Studio", Icons.Default.AutoAwesome),
  TEMPLATES("Template Builder", Icons.Default.Layers),
  PERMISSIONS("Access Control", Icons.Default.AdminPanelSettings)
}

@Composable
fun OwnerStudioScreen(
  viewModel: EditorViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableStateOf(OwnerStudioTab.EFFECTS) }
  val effectPresets by viewModel.effectPresets.collectAsState()
  val customTemplates by viewModel.customTemplates.collectAsState()
  val isOwnerMode by viewModel.isOwnerMode.collectAsState()

  // Creation Modals
  var showCreatePresetDialog by remember { mutableStateOf(false) }
  var showCreateTemplateDialog by remember { mutableStateOf(false) }

  BackHandler {
    onNavigateBack()
  }

  Scaffold(
    containerColor = CharcoalBackground,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp).testTag("owner_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
          }
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Owner Studio 👑", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.width(6.dp))
              Surface(shape = RoundedCornerShape(4.dp), color = GoldAccent.copy(alpha = 0.2f)) {
                Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldAccent, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
              }
            }
            Text("Architect Effects & Templates", style = MaterialTheme.typography.labelSmall, color = TextMuted)
          }
        }

        // Mode switch indicator
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isOwnerMode) EmeraldPrimary.copy(alpha = 0.15f) else CharcoalCard,
          border = BorderStroke(1.dp, if (isOwnerMode) EmeraldPrimary else CharcoalBorder),
          modifier = Modifier.clickable {
            val next = !isOwnerMode
            viewModel.setOwnerMode(next)
            Toast.makeText(context, if (next) "Owner Mode Activated 👑" else "Creator Mode Activated", Toast.LENGTH_SHORT).show()
          }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(if (isOwnerMode) EmeraldPrimary else TextMuted, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isOwnerMode) "Owner Live" else "User View", style = MaterialTheme.typography.labelSmall, color = if (isOwnerMode) EmeraldPrimary else TextMuted)
          }
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Navigation Tabs
      TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = CharcoalSurface,
        contentColor = EmeraldPrimary,
        indicator = {},
        divider = { Divider(color = CharcoalBorder) }
      ) {
        OwnerStudioTab.values().forEach { tab ->
          val isSelected = selectedTab == tab
          Tab(
            selected = isSelected,
            onClick = { selectedTab = tab },
            text = { Text(tab.displayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            icon = { Icon(tab.icon, contentDescription = tab.displayName, modifier = Modifier.size(16.dp)) },
            selectedContentColor = EmeraldPrimary,
            unselectedContentColor = TextMuted
          )
        }
      }

      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        when (selectedTab) {
          OwnerStudioTab.EFFECTS -> {
            EffectsStudioContent(
              presets = effectPresets,
              onCreateNewPreset = { showCreatePresetDialog = true },
              onTogglePublish = { id, publish -> viewModel.publishEffectPreset(id, publish) },
              onDelete = { id -> viewModel.deleteEffectPreset(id) },
              onDuplicate = { preset ->
                val copy = preset.copy(
                  id = "preset_${UUID.randomUUID().toString().take(6)}",
                  name = "${preset.name} (Copy)",
                  isDraft = true,
                  isPublished = false
                )
                viewModel.saveCustomEffectPreset(copy)
              }
            )
          }

          OwnerStudioTab.TEMPLATES -> {
            TemplateStudioContent(
              templates = customTemplates,
              onCreateNewTemplate = { showCreateTemplateDialog = true },
              onTogglePublish = { id, publish -> viewModel.publishCustomTemplate(id, publish) },
              onDelete = { id -> viewModel.deleteCustomTemplate(id) },
              onDuplicate = { template ->
                val copy = template.copy(
                  id = "tpl_${UUID.randomUUID().toString().take(6)}",
                  title = "${template.title} (Copy)",
                  isDraft = true,
                  isPublished = false
                )
                viewModel.saveCustomTemplate(copy)
              }
            )
          }

          OwnerStudioTab.PERMISSIONS -> {
            PermissionsStudioContent(
              isOwnerMode = isOwnerMode,
              onToggleOwnerMode = { viewModel.setOwnerMode(it) }
            )
          }
        }
      }
    }
  }

  // Dialog: Create New Effect Preset
  if (showCreatePresetDialog) {
    CreateEffectPresetDialog(
      onDismiss = { showCreatePresetDialog = false },
      onSave = { preset ->
        viewModel.saveCustomEffectPreset(preset)
        showCreatePresetDialog = false
        Toast.makeText(context, "Preset \"${preset.name}\" Created & Saved!", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Dialog: Create New Template
  if (showCreateTemplateDialog) {
    CreateTemplateDialog(
      onDismiss = { showCreateTemplateDialog = false },
      onSave = { template ->
        viewModel.saveCustomTemplate(template)
        showCreateTemplateDialog = false
        Toast.makeText(context, "Template \"${template.title}\" Created!", Toast.LENGTH_SHORT).show()
      }
    )
  }
}

@Composable
private fun EffectsStudioContent(
  presets: List<EffectPreset>,
  onCreateNewPreset: () -> Unit,
  onTogglePublish: (String, Boolean) -> Unit,
  onDelete: (String) -> Unit,
  onDuplicate: (EffectPreset) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Top Action Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Effect Presets Library", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text("${presets.size} Custom Presets Managed", style = MaterialTheme.typography.labelSmall, color = TextMuted)
      }

      Button(
        onClick = onCreateNewPreset,
        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = CharcoalBackground),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("create_preset_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("New Preset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // List of Presets
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      presets.forEach { preset ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, if (preset.isPublished) EmeraldPrimary.copy(alpha = 0.4f) else CharcoalBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = CircleShape,
                  color = CharcoalSurfaceVariant,
                  modifier = Modifier.size(36.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(preset.thumbnailIcon, fontSize = 18.sp)
                  }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(preset.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(preset.category, style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                    Text("•", color = TextMuted, fontSize = 10.sp)
                    Text("${preset.effects.size} layers", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                  }
                }
              }

              // Status Chip
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (preset.isPublished) EmeraldPrimary.copy(alpha = 0.2f) else Color(0x33FF9800),
                border = BorderStroke(1.dp, if (preset.isPublished) EmeraldPrimary else GoldAccent)
              ) {
                Text(
                  text = if (preset.isPublished) "Published" else "Draft",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (preset.isPublished) EmeraldPrimary else GoldAccent,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(preset.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            // Multi-effect layers preview chips
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              preset.effects.forEach { applied ->
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = CharcoalSurfaceVariant,
                  border = BorderStroke(0.5.dp, CharcoalBorder)
                ) {
                  Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterVintage, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "${applied.effect.displayName} (${(applied.intensity * 100).toInt()}%)",
                      fontSize = 9.sp,
                      color = TextPrimary
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = CharcoalBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Management Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Publish / Unpublish Toggle
                FilledTonalButton(
                  onClick = { onTogglePublish(preset.id, !preset.isPublished) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (preset.isPublished) Color(0x33FF334B) else Color(0x3300E676),
                    contentColor = if (preset.isPublished) PlayheadRed else EmeraldPrimary
                  ),
                  modifier = Modifier.height(28.dp)
                ) {
                  Text(if (preset.isPublished) "Unpublish" else "Publish Live", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Duplicate
                FilledTonalButton(
                  onClick = { onDuplicate(preset) },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = CharcoalSurfaceVariant, contentColor = TextPrimary),
                  modifier = Modifier.height(28.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("Duplicate", fontSize = 10.sp)
                }
              }

              IconButton(onClick = { onDelete(preset.id) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PlayheadRed, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TemplateStudioContent(
  templates: List<CustomTemplate>,
  onCreateNewTemplate: () -> Unit,
  onTogglePublish: (String, Boolean) -> Unit,
  onDelete: (String) -> Unit,
  onDuplicate: (CustomTemplate) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Official Templates", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text("${templates.size} Templates in Production", style = MaterialTheme.typography.labelSmall, color = TextMuted)
      }

      Button(
        onClick = onCreateNewTemplate,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("create_template_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("New Template", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      templates.forEach { tpl ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, if (tpl.isPublished) EmeraldPrimary.copy(alpha = 0.4f) else CharcoalBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(tpl.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(tpl.tagline, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (tpl.isPublished) EmeraldPrimary.copy(alpha = 0.2f) else Color(0x33FF9800),
                border = BorderStroke(1.dp, if (tpl.isPublished) EmeraldPrimary else GoldAccent)
              ) {
                Text(
                  text = if (tpl.isPublished) "Published" else "Draft",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (tpl.isPublished) EmeraldPrimary else GoldAccent,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Surface(shape = RoundedCornerShape(4.dp), color = CharcoalSurfaceVariant) {
                Text("Slots: ${tpl.requiredClipsCount} videos", fontSize = 10.sp, color = EmeraldPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
              Surface(shape = RoundedCornerShape(4.dp), color = CharcoalSurfaceVariant) {
                Text("${(tpl.durationMs / 1000f).toInt()}s", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
              Surface(shape = RoundedCornerShape(4.dp), color = CharcoalSurfaceVariant) {
                Text(tpl.aspectRatio.label, fontSize = 10.sp, color = CyanAccent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = CharcoalBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                  onClick = { onTogglePublish(tpl.id, !tpl.isPublished) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (tpl.isPublished) Color(0x33FF334B) else Color(0x3300E676),
                    contentColor = if (tpl.isPublished) PlayheadRed else EmeraldPrimary
                  ),
                  modifier = Modifier.height(28.dp)
                ) {
                  Text(if (tpl.isPublished) "Unpublish" else "Publish Live", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                  onClick = { onDuplicate(tpl) },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = CharcoalSurfaceVariant, contentColor = TextPrimary),
                  modifier = Modifier.height(28.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("Duplicate", fontSize = 10.sp)
                }
              }

              IconButton(onClick = { onDelete(tpl.id) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PlayheadRed, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PermissionsStudioContent(
  isOwnerMode: Boolean,
  onToggleOwnerMode: (Boolean) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = CharcoalCard,
      border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(shape = CircleShape, color = GoldAccent, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CharcoalBackground, modifier = Modifier.size(24.dp))
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Owner Access & Identity", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Authorized Master Administrator", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = CharcoalBorder)
        Spacer(modifier = Modifier.height(14.dp))

        Text("Registered Account Email", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text("mdwahid5112@gmail.com", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))
        Text("Studio Role & Permissions", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text("Full RW (Read/Write/Deploy) to Official Effects Catalog & Template Store", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Owner Override Mode", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Toggle to simulate regular creator user flow", style = MaterialTheme.typography.bodySmall, color = TextMuted)
          }
          Switch(
            checked = isOwnerMode,
            onCheckedChange = onToggleOwnerMode,
            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary, checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f))
          )
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(14.dp),
      color = CharcoalCard,
      border = BorderStroke(1.dp, CharcoalBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Security Architecture & Zero-Trust Rules", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text("• Official effect algorithms and definitions are immutable to regular end-users.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text("• End-users can adjust calibrated intensity and speed parameters locally without altering production presets.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text("• Owner presets and templates are validated and saved to persistent device storage.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
      }
    }
  }
}

@Composable
private fun CreateEffectPresetDialog(
  onDismiss: () -> Unit,
  onSave: (EffectPreset) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Cinematic") }
  var thumbnailIcon by remember { mutableStateOf("✨") }
  var isPublished by remember { mutableStateOf(true) }

  // Multi-effect stack selection
  val availableEffects = VideoEffect.values().filter { it != VideoEffect.NONE }
  var selectedEffects by remember {
    mutableStateOf(
      listOf(
        AppliedEffect(effect = VideoEffect.CINEMATIC_GLOW, intensity = 0.85f),
        AppliedEffect(effect = VideoEffect.SOFT_FOCUS, intensity = 0.60f)
      )
    )
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = CharcoalSurface,
      border = BorderStroke(1.dp, GoldAccent),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.88f)
        .padding(vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("New Multi-Effect Preset 👑", style = MaterialTheme.typography.titleLarge, color = GoldAccent, fontWeight = FontWeight.Bold)
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Preset Title") },
            placeholder = { Text("e.g. Royal Bollywood Glow") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, focusedTextColor = TextPrimary),
            modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
          )

          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Mood") },
            placeholder = { Text("e.g. Luminous highlight bloom and warm portrait response") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, focusedTextColor = TextPrimary),
            modifier = Modifier.fillMaxWidth()
          )

          // Category Chips
          Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Cinematic", "Glitch", "Light", "Retro", "Body & Motion", "Shake & Camera", "Particles", "Distortion").forEach { cat ->
              FilterChip(
                selected = category == cat,
                onClick = { category = cat },
                label = { Text(cat, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldAccent, selectedLabelColor = CharcoalBackground)
              )
            }
          }

          // Icon Picker
          Text("Thumbnail Symbol", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("✨", "👑", "⚡", "🌅", "📼", "👤", "🔥", "💎").forEach { sym ->
              FilterChip(
                selected = thumbnailIcon == sym,
                onClick = { thumbnailIcon = sym },
                label = { Text(sym, fontSize = 14.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldAccent, selectedLabelColor = CharcoalBackground)
              )
            }
          }

          // Multi-Effect Stack
          Text("Combined Effects Stack (${selectedEffects.size})", style = MaterialTheme.typography.labelSmall, color = GoldAccent, fontWeight = FontWeight.Bold)

          selectedEffects.forEachIndexed { idx, applied ->
            Surface(shape = RoundedCornerShape(8.dp), color = CharcoalCard, border = BorderStroke(1.dp, CharcoalBorder), modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                  Text("${idx + 1}. ${applied.effect.displayName}", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                  if (selectedEffects.size > 1) {
                    IconButton(onClick = { selectedEffects = selectedEffects.filterIndexed { i, _ -> i != idx } }, modifier = Modifier.size(20.dp)) {
                      Icon(Icons.Default.Close, contentDescription = null, tint = PlayheadRed, modifier = Modifier.size(14.dp))
                    }
                  }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Calibrated Intensity", fontSize = 10.sp, color = TextMuted)
                  Text("${(applied.intensity * 100).toInt()}%", fontSize = 10.sp, color = GoldAccent)
                }
                Slider(
                  value = applied.intensity,
                  onValueChange = { newInt ->
                    selectedEffects = selectedEffects.mapIndexed { i, old -> if (i == idx) old.copy(intensity = newInt) else old }
                  },
                  valueRange = 0.1f..1.0f,
                  colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                )
              }
            }
          }

          // Add Effect Layer Button
          OutlinedButton(
            onClick = {
              val randomNext = availableEffects.shuffled().first()
              selectedEffects = selectedEffects + AppliedEffect(effect = randomNext, intensity = 0.75f)
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
            border = BorderStroke(1.dp, GoldAccent),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Effect Layer To Preset", fontSize = 12.sp)
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Publish immediately to user catalog", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Switch(
              checked = isPublished,
              onCheckedChange = { isPublished = it },
              colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = GoldAccent.copy(alpha = 0.5f))
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (name.isNotBlank()) {
                onSave(
                  EffectPreset(
                    id = "preset_${UUID.randomUUID().toString().take(6)}",
                    name = name,
                    description = description.ifBlank { "Custom multi-effect combo designed in Owner Studio" },
                    category = category,
                    thumbnailIcon = thumbnailIcon,
                    effects = selectedEffects,
                    isPublished = isPublished
                  )
                )
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = CharcoalBackground),
            modifier = Modifier.testTag("save_preset_confirm")
          ) {
            Text("Save Preset", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun CreateTemplateDialog(
  onDismiss: () -> Unit,
  onSave: (CustomTemplate) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var tagline by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Trending") }
  var requiredClips by remember { mutableIntStateOf(3) }
  var durationSec by remember { mutableIntStateOf(8) }
  var bgmName by remember { mutableStateOf("Desi Beats High Octane (128 BPM)") }
  var isPublished by remember { mutableStateOf(true) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = CharcoalSurface,
      border = BorderStroke(1.dp, EmeraldPrimary),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("New Viral Template ✨", style = MaterialTheme.typography.titleLarge, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
          }
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Template Title") },
          placeholder = { Text("e.g. Bollywood Action Cut 🔥") },
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, focusedTextColor = TextPrimary),
          modifier = Modifier.fillMaxWidth().testTag("template_title_input")
        )

        OutlinedTextField(
          value = tagline,
          onValueChange = { tagline = it },
          label = { Text("Tagline / Vibe") },
          placeholder = { Text("e.g. High energy punch transitions & neon glows") },
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, focusedTextColor = TextPrimary),
          modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Required Videos: $requiredClips", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Slider(value = requiredClips.toFloat(), onValueChange = { requiredClips = it.toInt() }, valueRange = 2f..10f)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text("Duration: ${durationSec}s", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Slider(value = durationSec.toFloat(), onValueChange = { durationSec = it.toInt() }, valueRange = 4f..30f)
          }
        }

        OutlinedTextField(
          value = bgmName,
          onValueChange = { bgmName = it },
          label = { Text("BGM Track") },
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, focusedTextColor = TextPrimary),
          modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (title.isNotBlank()) {
                onSave(
                  CustomTemplate(
                    id = "tpl_${UUID.randomUUID().toString().take(6)}",
                    title = title,
                    tagline = tagline.ifBlank { "Official DesiCut Studio Template" },
                    category = category,
                    requiredClipsCount = requiredClips,
                    durationMs = (durationSec * 1000L),
                    bgmName = bgmName,
                    isPublished = isPublished
                  )
                )
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
            modifier = Modifier.testTag("save_template_confirm")
          ) {
            Text("Create Template", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
