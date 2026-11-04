package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.AudioTrack
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPickerSheet(
  onDismiss: () -> Unit,
  onAudioSelected: (AudioTrack) -> Unit,
  onImportCustomAudio: ((Uri) -> Unit)? = null
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Music, 1: Sound FX, 2: Voice Record
  var isRecording by remember { mutableStateOf(false) }
  var recordingSeconds by remember { mutableIntStateOf(0) }
  val coroutineScope = rememberCoroutineScope()

  val audioPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      onImportCustomAudio?.invoke(uri)
      onDismiss()
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = CharcoalSurface,
    dragHandle = { BottomSheetDefaults.DragHandle(color = CharcoalBorder) }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Audio Studio 🎵", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Button to import device audio
      OutlinedButton(
        onClick = { audioPickerLauncher.launch("audio/*") },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().testTag("import_device_audio_btn")
      ) {
        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Import Audio File from Device Storage", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Tab selector: Music | Sound FX | Voiceover
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = CharcoalCard,
        contentColor = EmeraldPrimary,
        indicator = {},
        divider = {}
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Music Library") }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Sound Effects") }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Voiceover Mic") }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      when (selectedTab) {
        0 -> {
          // Royalty free music tracks
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
          ) {
            items(SampleData.royaltyFreeAudio) { track ->
              AudioItemCard(
                track = track,
                onAdd = {
                  onAudioSelected(track)
                  onDismiss()
                }
              )
            }
          }
        }
        1 -> {
          // Sound Effects
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 300.dp)
          ) {
            items(SampleData.soundEffects) { track ->
              AudioItemCard(
                track = track,
                onAdd = {
                  onAudioSelected(track)
                  onDismiss()
                }
              )
            }
          }
        }
        2 -> {
          // Voiceover Recording Panel
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = if (isRecording) "Recording Voiceover... 🎙️" else "Record Voiceover to Timeline",
              style = MaterialTheme.typography.titleMedium,
              color = if (isRecording) ErrorRed else TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isRecording) "%02d:%02d".format(recordingSeconds / 60, recordingSeconds % 60) else "Tap to start recording commentary or dialog",
              style = MaterialTheme.typography.displayMedium,
              color = if (isRecording) EmeraldPrimary else TextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Mic Button
            val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
            val pulseScale by infiniteTransition.animateFloat(
              initialValue = 1f,
              targetValue = if (isRecording) 1.25f else 1f,
              animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
              ),
              label = "micScale"
            )

            FilledIconButton(
              onClick = {
                if (!isRecording) {
                  isRecording = true
                  recordingSeconds = 0
                  coroutineScope.launch {
                    while (isRecording) {
                      delay(1000)
                      recordingSeconds++
                    }
                  }
                } else {
                  isRecording = false
                  val recTrack = AudioTrack(
                    id = "voice_${UUID.randomUUID().toString().take(6)}",
                    title = "My Voiceover",
                    artist = "Recorded Voice",
                    category = "Voiceover",
                    durationMs = (recordingSeconds * 1000L).coerceAtLeast(2000L)
                  )
                  onAudioSelected(recTrack)
                  onDismiss()
                }
              },
              modifier = Modifier
                .size(72.dp)
                .testTag("record_voice_button"),
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isRecording) ErrorRed else EmeraldPrimary,
                contentColor = CharcoalBackground
              )
            ) {
              Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = "Record",
                modifier = Modifier.size(36.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AudioItemCard(
  track: AudioTrack,
  onAdd: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = CharcoalCard,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = EmeraldGlow,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
          }
        }
        Column {
          Text(track.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontSize = 14.sp)
          Text("${track.artist} • ${track.category} • ${(track.durationMs / 1000f).toInt()}s", style = MaterialTheme.typography.bodyMedium, color = TextMuted, fontSize = 12.sp)
        }
      }

      Button(
        onClick = onAdd,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(34.dp).testTag("add_audio_${track.id}")
      ) {
        Text("Add", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
      }
    }
  }
}
