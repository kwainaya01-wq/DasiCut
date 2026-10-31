package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
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
import com.example.model.AspectRatio
import com.example.model.Project
import com.example.model.TransitionType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.ToolTab

@Composable
fun EditorScreen(
  viewModel: EditorViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val project by viewModel.currentProject.collectAsState()
  val playheadMs by viewModel.playheadMs.collectAsState()
  val isPlaying by viewModel.isPlaying.collectAsState()
  val selectedClipId by viewModel.selectedClipId.collectAsState()
  val selectedAudioId by viewModel.selectedAudioId.collectAsState()
  val selectedTextId by viewModel.selectedTextId.collectAsState()
  val activeToolTab by viewModel.activeToolTab.collectAsState()
  val timelineZoom by viewModel.timelineZoom.collectAsState()
  val canUndo by viewModel.canUndo.collectAsState()
  val canRedo by viewModel.canRedo.collectAsState()
  val previewQuality by viewModel.previewQuality.collectAsState()

  // Export pipeline states
  val isExporting by viewModel.isExporting.collectAsState()
  val exportProgress by viewModel.exportProgress.collectAsState()
  val exportStage by viewModel.exportStage.collectAsState()
  val exportedFilePath by viewModel.exportedFilePath.collectAsState()

  // Sheet / Modal visibility
  var showExportDialog by remember { mutableStateOf(false) }
  var showAudioPicker by remember { mutableStateOf(false) }
  var showTextEditor by remember { mutableStateOf(false) }
  var showStickerPicker by remember { mutableStateOf(false) }
  var showRenameDialog by remember { mutableStateOf(false) }

  // Handle system back navigation
  BackHandler {
    viewModel.pausePlayback()
    onNavigateBack()
  }

  // Media picker to add custom clips
  val mediaPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      viewModel.addNewClip(
        title = "Gallery Video",
        colorStart = "#136A8A",
        colorEnd = "#267871",
        uri = uri.toString()
      )
    }
  }

  if (project == null) {
    Box(modifier = Modifier.fillMaxSize().background(CharcoalBackground), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = EmeraldPrimary)
    }
    return
  }

  val currentProj = project!!
  val selectedClip = currentProj.clips.find { it.id == selectedClipId }

  Scaffold(
    containerColor = CharcoalBackground,
    topBar = {
      EditorTopBar(
        projectName = currentProj.name,
        canUndo = canUndo,
        canRedo = canRedo,
        onBackClick = {
          viewModel.pausePlayback()
          onNavigateBack()
        },
        onRenameClick = { showRenameDialog = true },
        onUndoClick = { viewModel.undo() },
        onRedoClick = { viewModel.redo() },
        onExportClick = { showExportDialog = true }
      )
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Live Video Preview Canvas with real shader filters & effects
      VideoPreviewCanvas(
        project = currentProj,
        playheadMs = playheadMs,
        isPlaying = isPlaying,
        previewQuality = previewQuality,
        onTogglePlay = { viewModel.togglePlayback() },
        onStepBackward = { viewModel.setPlayheadMs(playheadMs - 100L) },
        onStepForward = { viewModel.setPlayheadMs(playheadMs + 100L) },
        onChangeRatioClick = { viewModel.setToolTab(ToolTab.CANVAS) },
        onChangeQualityClick = {
          val nextQ = if (previewQuality == "1080p Full") "720p Fast" else "1080p Full"
          viewModel.setPreviewQuality(nextQ)
        },
        modifier = Modifier.weight(1f)
      )

      // 2. Interactive Multi-Track Timeline (Ruler, Clips, Audio, Text, Playhead)
      TimelineView(
        project = currentProj,
        playheadMs = playheadMs,
        selectedClipId = selectedClipId,
        selectedAudioId = selectedAudioId,
        selectedTextId = selectedTextId,
        zoom = timelineZoom,
        onPlayheadChanged = { viewModel.setPlayheadMs(it) },
        onSelectClip = { viewModel.selectClip(it) },
        onSelectAudio = { viewModel.selectAudio(it) },
        onSelectText = { viewModel.selectText(it) },
        onOpenTransitions = {
          viewModel.selectClip(it)
          viewModel.setToolTab(ToolTab.TRANSITIONS)
        },
        onAddClipClick = {
          mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        },
        onSplitClick = { viewModel.splitSelectedClipAtPlayhead() },
        onZoomIn = { viewModel.setTimelineZoom(timelineZoom + 0.3f) },
        onZoomOut = { viewModel.setTimelineZoom(timelineZoom - 0.3f) }
      )

      // 3. Bottom Tool Sheet & Editing Action Bar
      ToolSheet(
        activeTab = activeToolTab,
        selectedClip = selectedClip,
        onTabSelected = { viewModel.setToolTab(it) },
        onSplitClick = { viewModel.splitSelectedClipAtPlayhead() },
        onTrimChanged = { startMs, endMs ->
          selectedClipId?.let { viewModel.trimClip(it, startMs, endMs) }
        },
        onSpeedChanged = { speed ->
          selectedClipId?.let { viewModel.updateClipSpeed(it, speed) }
        },
        onVolumeChanged = { vol, isMuted ->
          selectedClipId?.let { viewModel.updateClipVolume(it, vol, isMuted) }
        },
        onFilterChanged = { filter, intensity ->
          selectedClipId?.let { viewModel.updateClipFilter(it, filter, intensity) }
        },
        onEffectChanged = { effect, intensity ->
          selectedClipId?.let { viewModel.updateClipEffect(it, effect, intensity) }
        },
        onTransitionChanged = { trans ->
          selectedClipId?.let { viewModel.updateClipTransition(it, trans) }
        },
        onRotateClick = {
          selectedClipId?.let { viewModel.rotateClip(it) }
        },
        onFlipHClick = {
          selectedClipId?.let { viewModel.flipClipH(it) }
        },
        onDuplicateClick = {
          viewModel.duplicateSelectedClip()
        },
        onDeleteClick = {
          viewModel.deleteSelectedClip()
        },
        onReverseClick = {
          selectedClipId?.let { viewModel.reverseClip(it) }
        },
        onFreezeFrameClick = {
          selectedClipId?.let { viewModel.freezeFrame(it) }
        },
        onOpenAudioPicker = { showAudioPicker = true },
        onOpenTextEditor = { showTextEditor = true },
        onOpenStickerPicker = { showStickerPicker = true },
        onAutoCaptions = { viewModel.generateAutoCaptions() },
        onChangeRatio = { viewModel.setAspectRatio(it) }
      )
    }
  }

  // --- Modal Sheets & Dialogs ---

  // Export Dialog
  if (showExportDialog || isExporting || exportedFilePath != null) {
    ExportDialog(
      isExporting = isExporting,
      exportProgress = exportProgress,
      exportStage = exportStage,
      exportedFileName = exportedFilePath,
      onDismiss = {
        showExportDialog = false
        viewModel.dismissExport()
      },
      onStartExport = { settings ->
        viewModel.startExport(settings)
      }
    )
  }

  // Audio Picker Sheet
  if (showAudioPicker) {
    AudioPickerSheet(
      onDismiss = { showAudioPicker = false },
      onAudioSelected = { track ->
        viewModel.addAudioTrack(track)
      }
    )
  }

  // Text Editor Dialog
  if (showTextEditor) {
    TextEditorDialog(
      onDismiss = { showTextEditor = false },
      onSaveText = { text, colorHex, bgHex, animation, fontSize ->
        viewModel.addTextOverlay(text, colorHex, bgHex, animation)
      }
    )
  }

  // Sticker Picker Sheet
  if (showStickerPicker) {
    StickerPickerSheet(
      onDismiss = { showStickerPicker = false },
      onStickerSelected = { symbol, label ->
        viewModel.addSticker(symbol, label)
      }
    )
  }

  // Rename Project Dialog
  if (showRenameDialog) {
    AlertDialog(
      onDismissRequest = { showRenameDialog = false },
      title = { Text("Rename Project", color = TextPrimary) },
      text = {
        var tempName by remember { mutableStateOf(currentProj.name) }
        OutlinedTextField(
          value = tempName,
          onValueChange = { tempName = it },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldPrimary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )
      },
      containerColor = CharcoalSurface,
      confirmButton = {
        Button(
          onClick = { showRenameDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = CharcoalBackground)
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showRenameDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun EditorTopBar(
  projectName: String,
  canUndo: Boolean,
  canRedo: Boolean,
  onBackClick: () -> Unit,
  onRenameClick: () -> Unit,
  onUndoClick: () -> Unit,
  onRedoClick: () -> Unit,
  onExportClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Back Button & Project Title
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp).testTag("editor_back_button")) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
      }

      Spacer(modifier = Modifier.width(4.dp))

      Text(
        text = projectName,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
          .clickable { onRenameClick() }
          .padding(4.dp)
      )
    }

    // Undo / Redo & Export Action Pill
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onUndoClick,
        enabled = canUndo,
        modifier = Modifier.size(32.dp).testTag("editor_undo_button")
      ) {
        Icon(
          Icons.AutoMirrored.Filled.Undo,
          contentDescription = "Undo",
          tint = if (canUndo) TextPrimary else TextMuted.copy(alpha = 0.4f)
        )
      }

      IconButton(
        onClick = onRedoClick,
        enabled = canRedo,
        modifier = Modifier.size(32.dp).testTag("editor_redo_button")
      ) {
        Icon(
          Icons.AutoMirrored.Filled.Redo,
          contentDescription = "Redo",
          tint = if (canRedo) TextPrimary else TextMuted.copy(alpha = 0.4f)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Glowing Emerald Export Button
      Button(
        onClick = onExportClick,
        colors = ButtonDefaults.buttonColors(
          containerColor = EmeraldPrimary,
          contentColor = CharcoalBackground
        ),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier
          .height(34.dp)
          .testTag("editor_export_button")
      ) {
        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Export", fontWeight = FontWeight.Bold, fontSize = 13.sp)
      }
    }
  }
}
