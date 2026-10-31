package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorViewModel

@Composable
fun ProfileScreen(
  viewModel: EditorViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val projects by viewModel.projects.collectAsState()
  var defaultResolution by remember { mutableStateOf("1080p") }
  var defaultFps by remember { mutableIntStateOf(60) }
  var hapticFeedback by remember { mutableStateOf(true) }
  var autoSaveInterval by remember { mutableStateOf("Instant (Realtime)") }

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
        Text("Creator Studio 🇮🇳", style = MaterialTheme.typography.displayMedium, color = TextPrimary, fontSize = 24.sp)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder)
        ) {
          Text("v1.0 Pro", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Creator Profile Card
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = EmeraldPrimary,
              modifier = Modifier.size(56.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text("DC", style = MaterialTheme.typography.titleLarge, color = CharcoalBackground, fontWeight = FontWeight.Bold)
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Desi Creator Pro", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
              }
              Text("Free Pro Creator Tier Active", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
          }
        }
      }

      // Stats row
      item {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CharcoalCard,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("${projects.size}", style = MaterialTheme.typography.titleLarge, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
              Text("Projects", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("1080p", style = MaterialTheme.typography.titleLarge, color = GoldAccent, fontWeight = FontWeight.Bold)
              Text("Max Res", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("60 FPS", style = MaterialTheme.typography.titleLarge, color = CyanAccent, fontWeight = FontWeight.Bold)
              Text("Max Framerate", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
          }
        }
      }

      // Export Defaults Section
      item {
        Text("Export Defaults", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
      }

      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Default Resolution", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontSize = 14.sp)
                Text("Recommended for high definition mobile feed", style = MaterialTheme.typography.bodySmall, color = TextMuted)
              }
              FilterChip(
                selected = true,
                onClick = {
                  defaultResolution = if (defaultResolution == "1080p") "4K" else "1080p"
                },
                label = { Text(defaultResolution) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = CharcoalBackground)
              )
            }

            HorizontalDivider(color = CharcoalBorder)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Default Frame Rate", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontSize = 14.sp)
                Text("Buttery smooth reels playback", style = MaterialTheme.typography.bodySmall, color = TextMuted)
              }
              FilterChip(
                selected = true,
                onClick = {
                  defaultFps = if (defaultFps == 60) 30 else 60
                },
                label = { Text("${defaultFps} FPS") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = CharcoalBackground)
              )
            }

            HorizontalDivider(color = CharcoalBorder)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Timeline Haptic Feedback", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontSize = 14.sp)
                Text("Vibrate when snapping to clips and markers", style = MaterialTheme.typography.bodySmall, color = TextMuted)
              }
              Switch(
                checked = hapticFeedback,
                onCheckedChange = { hapticFeedback = it },
                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
              )
            }
          }
        }
      }

      // Storage Management
      item {
        Text("Storage Management", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
      }

      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CharcoalCard,
          border = BorderStroke(1.dp, CharcoalBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Local Project Storage", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontSize = 14.sp)
                Text("Persistent on-device project cache", style = MaterialTheme.typography.bodySmall, color = TextMuted)
              }
              Text("28.4 MB", style = MaterialTheme.typography.titleMedium, color = EmeraldPrimary)
            }

            OutlinedButton(
              onClick = {
                Toast.makeText(context, "Cache optimized and cleaned!", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Optimize & Clean Cache")
            }
          }
        }
      }

      // About
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = CharcoalSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("DesiCut Studio 🇮🇳", style = MaterialTheme.typography.titleLarge, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            Text("Create. Edit. Inspire.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Built for Indian Creators • Mobile-First Video Architecture", style = MaterialTheme.typography.labelSmall, color = TextMuted)
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
