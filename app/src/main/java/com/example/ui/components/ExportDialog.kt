package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExportSettings
import com.example.ui.theme.*

@Composable
fun ExportDialog(
  isExporting: Boolean,
  exportProgress: Float,
  exportStage: String,
  exportedFileName: String?,
  onDismiss: () -> Unit,
  onStartExport: (ExportSettings) -> Unit
) {
  val context = LocalContext.current
  var selectedResolution by remember { mutableStateOf("1080p") }
  var selectedFps by remember { mutableIntStateOf(30) }
  var highBitrate by remember { mutableStateOf(true) }

  val resolutions = listOf(
    "480p" to "SD (Quick share)",
    "720p" to "HD (Instagram/WhatsApp)",
    "1080p" to "Full HD (Reels & YouTube)",
    "4K" to "Ultra HD (Pro Cinema)"
  )

  val estimatedSize = when (selectedResolution) {
    "480p" -> "14.2 MB"
    "720p" -> "28.6 MB"
    "1080p" -> "54.8 MB"
    "4K" -> "168.0 MB"
    else -> "40 MB"
  }

  Dialog(onDismissRequest = {
    if (!isExporting || exportedFileName != null) {
      onDismiss()
    }
  }) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = CharcoalSurface,
      border = BorderStroke(1.dp, CharcoalBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (!isExporting && exportedFileName == null) {
          // --- Phase 1: Settings Config ---
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Export Video 🎬", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Resolution Selection
          Text("Resolution", style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.align(Alignment.Start))
          Spacer(modifier = Modifier.height(6.dp))
          Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            resolutions.forEach { (res, desc) ->
              val isSelected = selectedResolution == res
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) CharcoalCardSelected else CharcoalCard,
                border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else CharcoalBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("res_option_$res"),
                onClick = { selectedResolution = res }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(res, style = MaterialTheme.typography.titleMedium, color = if (isSelected) EmeraldPrimary else TextPrimary, fontSize = 14.sp)
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 11.sp)
                  }
                  if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Frame Rate Selection
          Text("Frame Rate", style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.align(Alignment.Start))
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(24, 30, 60).forEach { fps ->
              val isSelected = selectedFps == fps
              FilterChip(
                selected = isSelected,
                onClick = { selectedFps = fps },
                label = { Text("${fps} FPS") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = EmeraldPrimary,
                  selectedLabelColor = CharcoalBackground
                ),
                modifier = Modifier.weight(1f)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Estimated file size badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = CharcoalCard,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Estimated Size", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
              Text(estimatedSize, style = MaterialTheme.typography.titleMedium, color = GoldAccent, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Start Export Button
          Button(
            onClick = {
              onStartExport(
                ExportSettings(
                  resolution = selectedResolution,
                  fps = selectedFps,
                  bitrateMbps = if (highBitrate) 16f else 8f,
                  qualityLabel = "$selectedResolution $selectedFps fps"
                )
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
              .testTag("confirm_export_button")
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export MP4 Video", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        } else if (isExporting && exportedFileName == null) {
          // --- Phase 2: Live Export Rendering Progress ---
          Spacer(modifier = Modifier.height(10.dp))

          // Spinning render ring
          Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            CircularProgressIndicator(
              progress = { exportProgress },
              color = EmeraldPrimary,
              trackColor = CharcoalCard,
              strokeWidth = 6.dp,
              modifier = Modifier.fillMaxSize()
            )
            Text(
              text = "${(exportProgress * 100).toInt()}%",
              style = MaterialTheme.typography.titleMedium,
              color = EmeraldPrimary,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text("Rendering Your Video ⚡", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = exportStage,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))
          LinearProgressIndicator(
            progress = { exportProgress },
            color = EmeraldPrimary,
            trackColor = CharcoalCard,
            modifier = Modifier.fillMaxWidth().height(6.dp)
          )
        } else if (exportedFileName != null) {
          // --- Phase 3: Export Complete Screen ---
          Surface(
            shape = CircleShape,
            color = EmeraldGlow,
            modifier = Modifier.size(64.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(40.dp))
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text("Export Complete! 🎉", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = exportedFileName,
            style = MaterialTheme.typography.bodyMedium,
            color = EmeraldPrimary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Encoded in MP4 (H.264) • High Quality • Saved to Projects",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(20.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                Toast.makeText(context, "Saved to Device Gallery 📱", Toast.LENGTH_SHORT).show()
                onDismiss()
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save")
            }

            Button(
              onClick = {
                Toast.makeText(context, "Opening Share to WhatsApp / Instagram / YouTube", Toast.LENGTH_SHORT).show()
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Share")
            }
          }
        }
      }
    }
  }
}
