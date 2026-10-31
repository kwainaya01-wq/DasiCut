package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.EditorViewModel

data class AiToolItem(
  val id: String,
  val title: String,
  val description: String,
  val badge: String,
  val icon: ImageVector,
  val accentColor: Color
)

@Composable
fun AiLabScreen(
  viewModel: EditorViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var activeDialogTool by remember { mutableStateOf<AiToolItem?>(null) }

  val aiTools = listOf(
    AiToolItem(
      id = "captions",
      title = "AI Auto Captions 💬",
      description = "Generates synchronized Hinglish, Hindi & English animated subtitles matching video speech.",
      badge = "POPULAR",
      icon = Icons.Default.Subtitles,
      accentColor = EmeraldPrimary
    ),
    AiToolItem(
      id = "bg_remove",
      title = "AI Background Remover 🪄",
      description = "Removes background without green screen using edge detection and subject isolation.",
      badge = "NEURAL",
      icon = Icons.Default.AutoFixHigh,
      accentColor = CyanAccent
    ),
    AiToolItem(
      id = "voiceover",
      title = "AI Voiceover Studio 🎙️",
      description = "Transforms text scripts into natural Indian accent voices (Desi RJ, Male, Female, Cinematic Narrator).",
      badge = "VOICE",
      icon = Icons.Default.RecordVoiceOver,
      accentColor = GoldAccent
    ),
    AiToolItem(
      id = "beat_sync",
      title = "Smart Beat Sync ⚡",
      description = "Detects musical transients & drum hits to auto-cut clips on every bass drop.",
      badge = "AUDIO",
      icon = Icons.Default.GraphicEq,
      accentColor = NeonPink
    ),
    AiToolItem(
      id = "upscale",
      title = "AI 4K HDR Upscaler 🌟",
      description = "Enhances low-res mobile clips with neural super-resolution and noise reduction.",
      badge = "HD PRO",
      icon = Icons.Default.HighQuality,
      accentColor = EmeraldLight
    ),
    AiToolItem(
      id = "script_to_video",
      title = "AI Script to Video 🎬",
      description = "Turns ideas into storyboard scenes with placeholder timing and visual hooks.",
      badge = "STUDIO",
      icon = Icons.Default.Psychology,
      accentColor = SaffronAccent
    )
  )

  Scaffold(
    containerColor = CharcoalBackground,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("AI Lab ⚡", style = MaterialTheme.typography.displayMedium, color = TextPrimary, fontSize = 24.sp)
          Text("Neural multimedia intelligence tools", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldGlow,
          border = BorderStroke(1.dp, EmeraldPrimary)
        ) {
          Text(
            text = "AI ENGINE ACTIVE",
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = EmeraldGlow,
              modifier = Modifier.size(42.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
              }
            }
            Column {
              Text("On-Device Neural Acceleration", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontSize = 14.sp)
              Text("Optimized for smooth real-time video processing on Android.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
          }
        }
      }

      items(aiTools) { tool ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { activeDialogTool = tool }
            .testTag("ai_tool_${tool.id}")
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.weight(1f),
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = tool.accentColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, tool.accentColor.copy(alpha = 0.4f)),
                modifier = Modifier.size(48.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(tool.icon, contentDescription = null, tint = tool.accentColor, modifier = Modifier.size(24.dp))
                }
              }

              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(tool.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = tool.accentColor.copy(alpha = 0.2f)
                  ) {
                    Text(
                      text = tool.badge,
                      color = tool.accentColor,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(tool.description, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 12.sp)
              }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // Interactive AI Tool execution dialog
  activeDialogTool?.let { tool ->
    AlertDialog(
      onDismissRequest = { activeDialogTool = null },
      title = { Text(tool.title, color = TextPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(tool.description, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = CharcoalSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
          ) {
            Text(
              text = "Ready to apply ${tool.title} to your project timeline.",
              color = tool.accentColor,
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      containerColor = CharcoalSurface,
      confirmButton = {
        Button(
          onClick = {
            activeDialogTool = null
            when (tool.id) {
              "captions" -> {
                val proj = viewModel.projects.value.firstOrNull() ?: viewModel.repository.createNewProject()
                viewModel.openProject(proj)
                viewModel.generateAutoCaptions()
              }
              else -> {
                val proj = viewModel.projects.value.firstOrNull() ?: viewModel.repository.createNewProject()
                viewModel.openProject(proj)
                Toast.makeText(context, "${tool.title} applied to project!", Toast.LENGTH_SHORT).show()
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground)
        ) {
          Text("Launch in Editor", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { activeDialogTool = null }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}
