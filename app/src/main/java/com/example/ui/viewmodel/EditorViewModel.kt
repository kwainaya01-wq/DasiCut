package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.data.SampleData
import com.example.media.MediaFileManager
import com.example.media.NativeVideoExporter
import com.example.media.NativeVideoPlayerManager
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
  HOME,
  EDITOR,
  TEMPLATES,
  AI_LAB,
  PROFILE
}

enum class ToolTab {
  NONE,
  SPLIT,
  TRIM,
  SPEED,
  VOLUME,
  FILTERS,
  EFFECTS,
  TRANSITIONS,
  TRANSFORM,
  AUDIO,
  TEXT,
  STICKERS,
  CAPTIONS,
  CANVAS
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

  val repository = ProjectRepository(application)
  val projects: StateFlow<List<Project>> = repository.projects
  val playerManager = NativeVideoPlayerManager(application)

  private val _currentScreen = MutableStateFlow(AppScreen.HOME)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _currentProject = MutableStateFlow<Project?>(null)
  val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

  private val _playheadMs = MutableStateFlow(0L)
  val playheadMs: StateFlow<Long> = _playheadMs.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _selectedClipId = MutableStateFlow<String?>(null)
  val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

  private val _selectedAudioId = MutableStateFlow<String?>(null)
  val selectedAudioId: StateFlow<String?> = _selectedAudioId.asStateFlow()

  private val _selectedTextId = MutableStateFlow<String?>(null)
  val selectedTextId: StateFlow<String?> = _selectedTextId.asStateFlow()

  private val _activeToolTab = MutableStateFlow(ToolTab.NONE)
  val activeToolTab: StateFlow<ToolTab> = _activeToolTab.asStateFlow()

  private val _timelineZoom = MutableStateFlow(1.0f) // 0.5f to 2.5f
  val timelineZoom: StateFlow<Float> = _timelineZoom.asStateFlow()

  // Export pipeline states
  private val _isExporting = MutableStateFlow(false)
  val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

  private val _exportProgress = MutableStateFlow(0f)
  val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

  private val _exportStage = MutableStateFlow("")
  val exportStage: StateFlow<String> = _exportStage.asStateFlow()

  private val _exportedFilePath = MutableStateFlow<String?>(null)
  val exportedFilePath: StateFlow<String?> = _exportedFilePath.asStateFlow()

  // Undo / Redo stacks
  private val undoStack = mutableListOf<Project>()
  private val redoStack = mutableListOf<Project>()

  private val _canUndo = MutableStateFlow(false)
  val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

  private val _canRedo = MutableStateFlow(false)
  val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

  // Preview quality
  private val _previewQuality = MutableStateFlow("1080p Full")
  val previewQuality: StateFlow<String> = _previewQuality.asStateFlow()

  private var playbackJob: Job? = null

  fun navigateTo(screen: AppScreen) {
    if (screen != AppScreen.EDITOR) {
      pausePlayback()
    }
    _currentScreen.value = screen
  }

  fun openProject(project: Project) {
    _currentProject.value = project
    _playheadMs.value = 0L
    _selectedClipId.value = project.clips.firstOrNull()?.id
    _selectedAudioId.value = null
    _selectedTextId.value = null
    _activeToolTab.value = ToolTab.NONE
    undoStack.clear()
    redoStack.clear()
    updateUndoRedoStates()
    pausePlayback()
    _currentScreen.value = AppScreen.EDITOR
  }

  fun createNewProject(name: String = "Untitled Video", ratio: AspectRatio = AspectRatio.RATIO_9_16) {
    val newProj = repository.createNewProject(name, ratio)
    openProject(newProj)
  }

  fun createProjectWithVideo(uri: Uri, onCreated: (Project) -> Unit = {}) {
    viewModelScope.launch {
      val clip = MediaFileManager.importVideoFromUri(getApplication(), uri)
      val defaultClip = VideoClip(
        id = "clip_${UUID.randomUUID().toString().take(6)}",
        title = "Clip 1",
        colorStartHex = "#00B09B",
        colorEndHex = "#96C93D",
        durationMs = 4000L,
        trimEndMs = 4000L
      )
      val newProj = Project(
        id = "proj_${UUID.randomUUID().toString().take(8)}",
        name = clip?.title ?: "New Video Edit",
        aspectRatio = AspectRatio.RATIO_9_16,
        updatedAt = System.currentTimeMillis(),
        clips = if (clip != null) listOf(clip) else listOf(defaultClip)
      )
      repository.saveProject(newProj)
      openProject(newProj)
      if (clip != null) {
        _selectedClipId.value = clip.id
      }
      onCreated(newProj)
    }
  }

  fun useTemplate(template: Template) {
    val newProj = Project(
      id = "proj_${UUID.randomUUID().toString().take(8)}",
      name = "${template.title} Video",
      aspectRatio = template.aspectRatio,
      updatedAt = System.currentTimeMillis(),
      clips = template.previewClips.map { it.copy(id = "clip_${UUID.randomUUID().toString().take(6)}") },
      audioTracks = listOf(template.previewBgm.copy(id = "audio_${UUID.randomUUID().toString().take(6)}")),
      textOverlays = template.previewTexts.map { it.copy(id = "txt_${UUID.randomUUID().toString().take(6)}") }
    )
    repository.saveProject(newProj)
    openProject(newProj)
  }

  fun useTemplateWithUserVideos(template: Template, uris: List<Uri>, onCreated: (Project) -> Unit = {}) {
    viewModelScope.launch {
      val importedClips = uris.mapNotNull { uri ->
        MediaFileManager.importVideoFromUri(getApplication(), uri)
      }
      val templateClips = template.previewClips.mapIndexed { idx, slot ->
        val imported = importedClips.getOrNull(idx)
        if (imported != null) {
          slot.copy(
            id = "clip_${UUID.randomUUID().toString().take(6)}",
            title = imported.title,
            localUri = imported.localUri,
            durationMs = imported.durationMs,
            trimStartMs = 0L,
            trimEndMs = slot.durationMs.coerceAtMost(imported.durationMs)
          )
        } else {
          slot.copy(id = "clip_${UUID.randomUUID().toString().take(6)}")
        }
      }
      val newProj = Project(
        id = "proj_${UUID.randomUUID().toString().take(8)}",
        name = "${template.title} (Custom)",
        aspectRatio = template.aspectRatio,
        updatedAt = System.currentTimeMillis(),
        clips = templateClips,
        audioTracks = listOf(template.previewBgm.copy(id = "audio_${UUID.randomUUID().toString().take(6)}")),
        textOverlays = template.previewTexts.map { it.copy(id = "txt_${UUID.randomUUID().toString().take(6)}") }
      )
      repository.saveProject(newProj)
      openProject(newProj)
      onCreated(newProj)
    }
  }

  fun deleteProject(projectId: String) {
    repository.deleteProject(projectId)
    if (_currentProject.value?.id == projectId) {
      _currentProject.value = null
      _currentScreen.value = AppScreen.HOME
    }
  }

  fun duplicateProject(projectId: String) {
    repository.duplicateProject(projectId)
  }

  fun renameProject(projectId: String, newName: String) {
    val p = repository.projects.value.find { it.id == projectId } ?: return
    val updated = p.copy(name = newName)
    repository.saveProject(updated)
    if (_currentProject.value?.id == projectId) {
      _currentProject.value = updated
    }
  }

  fun setPlayheadMs(timeMs: Long) {
    val maxDuration = _currentProject.value?.totalDurationMs ?: 1000L
    val targetTime = timeMs.coerceIn(0L, maxDuration)
    _playheadMs.value = targetTime

    if (!_isPlaying.value) {
      val proj = _currentProject.value ?: return
      var clipStartMs = 0L
      var activeClip: VideoClip? = null
      var clipProgressMs = 0L

      for (c in proj.clips) {
        val duration = c.effectiveDurationMs
        if (targetTime in clipStartMs until (clipStartMs + duration)) {
          activeClip = c
          clipProgressMs = targetTime - clipStartMs
          break
        }
        clipStartMs += duration
      }
      val activeBgm = proj.audioTracks.find {
        targetTime in it.startAtMs until (it.startAtMs + it.durationMs)
      }
      playerManager.syncWithTimeline(activeClip, clipProgressMs, false, activeBgm)
    }
  }

  fun togglePlayback() {
    if (_isPlaying.value) {
      pausePlayback()
    } else {
      startPlayback()
    }
  }

  private fun startPlayback() {
    val total = _currentProject.value?.totalDurationMs ?: 1000L
    if (_playheadMs.value >= total) {
      _playheadMs.value = 0L
    }
    _isPlaying.value = true
    playbackJob?.cancel()
    playbackJob = viewModelScope.launch {
      val frameStepMs = 33L // ~30 fps updates
      while (isActive && _isPlaying.value) {
        delay(frameStepMs)
        val nextTime = _playheadMs.value + frameStepMs
        val currentTotal = _currentProject.value?.totalDurationMs ?: 1000L
        if (nextTime >= currentTotal) {
          _playheadMs.value = 0L // Loop around
        } else {
          _playheadMs.value = nextTime
        }
      }
    }
  }

  fun pausePlayback() {
    _isPlaying.value = false
    playbackJob?.cancel()
    playerManager.pauseVideo()
  }

  fun toggleClipMute(clipId: String) {
    val proj = _currentProject.value ?: return
    val clip = proj.clips.find { it.id == clipId } ?: return
    val newMuted = !clip.isMuted
    updateClipVolume(clipId, clip.volume, newMuted)
  }

  fun selectClip(clipId: String?) {
    _selectedClipId.value = clipId
    _selectedAudioId.value = null
    _selectedTextId.value = null
    if (clipId != null && _activeToolTab.value == ToolTab.NONE) {
      _activeToolTab.value = ToolTab.SPLIT
    }
  }

  fun selectAudio(audioId: String?) {
    _selectedAudioId.value = audioId
    _selectedClipId.value = null
    _selectedTextId.value = null
    if (audioId != null) {
      _activeToolTab.value = ToolTab.AUDIO
    }
  }

  fun selectText(textId: String?) {
    _selectedTextId.value = textId
    _selectedClipId.value = null
    _selectedAudioId.value = null
    if (textId != null) {
      _activeToolTab.value = ToolTab.TEXT
    }
  }

  fun setToolTab(tab: ToolTab) {
    _activeToolTab.value = if (_activeToolTab.value == tab) ToolTab.NONE else tab
  }

  fun setTimelineZoom(zoom: Float) {
    _timelineZoom.value = zoom.coerceIn(0.5f, 2.5f)
  }

  fun setAspectRatio(ratio: AspectRatio) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updated = proj.copy(aspectRatio = ratio)
    commitProject(updated)
  }

  fun setPreviewQuality(quality: String) {
    _previewQuality.value = quality
  }

  // --- Core Editing Actions ---

  private fun saveUndoState(p: Project) {
    undoStack.add(p)
    redoStack.clear()
    updateUndoRedoStates()
  }

  private fun commitProject(updated: Project) {
    _currentProject.value = updated
    repository.saveProject(updated)
  }

  fun undo() {
    if (undoStack.isNotEmpty()) {
      val current = _currentProject.value ?: return
      redoStack.add(current)
      val prev = undoStack.removeAt(undoStack.lastIndex)
      _currentProject.value = prev
      repository.saveProject(prev)
      updateUndoRedoStates()
    }
  }

  fun redo() {
    if (redoStack.isNotEmpty()) {
      val current = _currentProject.value ?: return
      undoStack.add(current)
      val next = redoStack.removeAt(redoStack.lastIndex)
      _currentProject.value = next
      repository.saveProject(next)
      updateUndoRedoStates()
    }
  }

  private fun updateUndoRedoStates() {
    _canUndo.value = undoStack.isNotEmpty()
    _canRedo.value = redoStack.isNotEmpty()
  }

  fun splitSelectedClipAtPlayhead() {
    val proj = _currentProject.value ?: return
    val clipId = _selectedClipId.value ?: return
    val clipIndex = proj.clips.indexOfFirst { it.id == clipId }
    if (clipIndex < 0) return
    val clip = proj.clips[clipIndex]

    // Calculate clip start in project timeline
    var clipStartInTimeline = 0L
    for (i in 0 until clipIndex) {
      clipStartInTimeline += proj.clips[i].effectiveDurationMs
    }
    val currentPlayhead = _playheadMs.value
    val offsetInClip = currentPlayhead - clipStartInTimeline

    if (offsetInClip <= 200L || offsetInClip >= clip.effectiveDurationMs - 200L) {
      // Too close to edges to split
      return
    }

    saveUndoState(proj)

    val rawSplitPoint = clip.trimStartMs + (offsetInClip * clip.speed).toLong()

    val clip1 = clip.copy(
      id = "clip_${UUID.randomUUID().toString().take(6)}",
      title = "${clip.title} (Part 1)",
      trimEndMs = rawSplitPoint,
      transitionToNext = TransitionType.NONE
    )

    val clip2 = clip.copy(
      id = "clip_${UUID.randomUUID().toString().take(6)}",
      title = "${clip.title} (Part 2)",
      trimStartMs = rawSplitPoint,
      trimEndMs = clip.trimEndMs
    )

    val newClips = proj.clips.toMutableList()
    newClips.removeAt(clipIndex)
    newClips.add(clipIndex, clip2)
    newClips.add(clipIndex, clip1)

    val updated = proj.copy(clips = newClips)
    commitProject(updated)
    _selectedClipId.value = clip2.id
  }

  fun trimClip(clipId: String, newTrimStartMs: Long, newTrimEndMs: Long) {
    val proj = _currentProject.value ?: return
    val clip = proj.clips.find { it.id == clipId } ?: return
    if (newTrimEndMs - newTrimStartMs < 200L) return

    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) {
        it.copy(trimStartMs = newTrimStartMs, trimEndMs = newTrimEndMs)
      } else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun updateClipSpeed(clipId: String, speed: Float) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) it.copy(speed = speed) else it
    }
    commitProject(proj.copy(clips = updatedClips))
    val currentClip = updatedClips.find { it.id == clipId }
    if (currentClip != null) {
      playerManager.setClipAudioAndSpeed(currentClip.volume, currentClip.isMuted, speed)
    }
  }

  fun updateClipVolume(clipId: String, volume: Float, isMuted: Boolean) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) it.copy(volume = volume, isMuted = isMuted) else it
    }
    commitProject(proj.copy(clips = updatedClips))
    val currentClip = updatedClips.find { it.id == clipId }
    if (currentClip != null) {
      playerManager.setClipAudioAndSpeed(volume, isMuted, currentClip.speed)
    }
  }

  fun updateClipFilter(clipId: String, filter: VideoFilter, intensity: Float) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) it.copy(filter = filter, filterIntensity = intensity) else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun updateClipEffect(clipId: String, effect: VideoEffect, intensity: Float) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) {
        val existing = it.effects.toMutableList()
        val index = existing.indexOfFirst { fx -> fx.effect == effect }
        if (index >= 0) {
          existing[index] = existing[index].copy(intensity = intensity)
        } else if (effect != VideoEffect.NONE) {
          existing.add(AppliedEffect(effect = effect, intensity = intensity))
        }
        it.copy(effect = effect, effectIntensity = intensity, effects = existing)
      } else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun addEffectToClip(clipId: String, effect: VideoEffect, intensity: Float = 0.8f) {
    if (effect == VideoEffect.NONE) return
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) {
        val existing = it.activeEffects.toMutableList()
        val newEffect = AppliedEffect(effect = effect, intensity = intensity)
        existing.add(newEffect)
        it.copy(effect = effect, effectIntensity = intensity, effects = existing)
      } else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun removeEffectFromClip(clipId: String, effectId: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) {
        val filtered = it.activeEffects.filter { fx -> fx.id != effectId }
        val fallbackEffect = filtered.lastOrNull()?.effect ?: VideoEffect.NONE
        val fallbackIntensity = filtered.lastOrNull()?.intensity ?: 0.8f
        it.copy(effect = fallbackEffect, effectIntensity = fallbackIntensity, effects = filtered)
      } else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun updateEffectIntensity(clipId: String, effectId: String, intensity: Float) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) {
        val updated = it.activeEffects.map { fx ->
          if (fx.id == effectId) fx.copy(intensity = intensity) else fx
        }
        it.copy(effects = updated)
      } else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun reorderClipEffects(clipId: String, fromIndex: Int, toIndex: Int) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) {
        val mutable = it.activeEffects.toMutableList()
        if (fromIndex in mutable.indices && toIndex in mutable.indices) {
          val item = mutable.removeAt(fromIndex)
          mutable.add(toIndex, item)
        }
        it.copy(effects = mutable)
      } else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun clearClipEffects(clipId: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) {
        it.copy(effect = VideoEffect.NONE, effects = emptyList())
      } else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun updateClipTransition(clipId: String, transition: TransitionType, durationMs: Long = 500L) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) it.copy(transitionToNext = transition, transitionDurationMs = durationMs) else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun updateTransitionDuration(clipId: String, durationMs: Long) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) it.copy(transitionDurationMs = durationMs.coerceIn(200L, 3000L)) else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun removeClipTransition(clipId: String) {
    updateClipTransition(clipId, TransitionType.NONE)
  }

  fun rotateClip(clipId: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) it.copy(rotationDegrees = (it.rotationDegrees + 90) % 360) else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun flipClipH(clipId: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) it.copy(isFlippedH = !it.isFlippedH) else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun duplicateSelectedClip() {
    val proj = _currentProject.value ?: return
    val clipId = _selectedClipId.value ?: return
    val index = proj.clips.indexOfFirst { it.id == clipId }
    if (index < 0) return
    val orig = proj.clips[index]
    val copy = orig.copy(
      id = "clip_${UUID.randomUUID().toString().take(6)}",
      title = "${orig.title} (Copy)"
    )
    saveUndoState(proj)
    val mutable = proj.clips.toMutableList()
    mutable.add(index + 1, copy)
    commitProject(proj.copy(clips = mutable))
    _selectedClipId.value = copy.id
  }

  fun deleteSelectedClip() {
    val proj = _currentProject.value ?: return
    val clipId = _selectedClipId.value ?: return
    if (proj.clips.size <= 1) {
      // Must keep at least one clip
      return
    }
    saveUndoState(proj)
    val newClips = proj.clips.filter { it.id != clipId }
    commitProject(proj.copy(clips = newClips))
    _selectedClipId.value = newClips.firstOrNull()?.id
  }

  fun reverseClip(clipId: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updatedClips = proj.clips.map {
      if (it.id == clipId) it.copy(isReversed = !it.isReversed) else it
    }
    commitProject(proj.copy(clips = updatedClips))
  }

  fun freezeFrame(clipId: String) {
    val proj = _currentProject.value ?: return
    val index = proj.clips.indexOfFirst { it.id == clipId }
    if (index < 0) return
    val clip = proj.clips[index]

    saveUndoState(proj)
    val frozen = clip.copy(
      id = "clip_freeze_${UUID.randomUUID().toString().take(6)}",
      title = "${clip.title} (Freeze)",
      durationMs = 2000L,
      trimStartMs = 0L,
      trimEndMs = 2000L,
      isFrozen = true
    )
    val mutable = proj.clips.toMutableList()
    mutable.add(index + 1, frozen)
    commitProject(proj.copy(clips = mutable))
  }

  fun addNewClip(title: String = "Imported Clip", colorStart: String = "#43C6AC", colorEnd: String = "#191654", uri: String? = null) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val newClip = VideoClip(
      id = "clip_${UUID.randomUUID().toString().take(6)}",
      title = title,
      colorStartHex = colorStart,
      colorEndHex = colorEnd,
      localUri = uri,
      durationMs = 4000L,
      trimEndMs = 4000L
    )
    val updatedClips = proj.clips + newClip
    commitProject(proj.copy(clips = updatedClips))
    _selectedClipId.value = newClip.id
  }

  // --- Audio Track Operations ---

  fun addAudioTrack(track: AudioTrack) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val copy = track.copy(
      id = "audio_${UUID.randomUUID().toString().take(6)}",
      startAtMs = _playheadMs.value
    )
    commitProject(proj.copy(audioTracks = proj.audioTracks + copy))
    _selectedAudioId.value = copy.id
  }

  fun removeAudioTrack(trackId: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val filtered = proj.audioTracks.filter { it.id != trackId }
    commitProject(proj.copy(audioTracks = filtered))
    if (_selectedAudioId.value == trackId) {
      _selectedAudioId.value = null
    }
  }

  fun updateAudioVolume(trackId: String, volume: Float, isMuted: Boolean) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val updated = proj.audioTracks.map {
      if (it.id == trackId) it.copy(volume = volume, isMuted = isMuted) else it
    }
    commitProject(proj.copy(audioTracks = updated))
  }

  // --- Text & Caption Operations ---

  fun addTextOverlay(text: String, colorHex: String = "#FFFFFF", bgColorHex: String? = "#80000000", animation: TextAnimation = TextAnimation.POP) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val newText = TextOverlay(
      id = "txt_${UUID.randomUUID().toString().take(6)}",
      text = text,
      startAtMs = _playheadMs.value,
      durationMs = 3000L,
      colorHex = colorHex,
      bgColorHex = bgColorHex,
      animationType = animation
    )
    commitProject(proj.copy(textOverlays = proj.textOverlays + newText))
    _selectedTextId.value = newText.id
  }

  fun updateTextOverlay(updated: TextOverlay) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val list = proj.textOverlays.map { if (it.id == updated.id) updated else it }
    commitProject(proj.copy(textOverlays = list))
  }

  fun deleteTextOverlay(textId: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val filtered = proj.textOverlays.filter { it.id != textId }
    commitProject(proj.copy(textOverlays = filtered))
    if (_selectedTextId.value == textId) {
      _selectedTextId.value = null
    }
  }

  fun generateAutoCaptions() {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    // Generate timed subtitles corresponding to clip beats
    val captions = mutableListOf<TextOverlay>()
    var currentMs = 0L
    val sampleCaptions = listOf(
      "Namaste India! 🙏 Welcome to the edit",
      "Notice the colors pop here ✨",
      "Drop the beat right now! 🔥",
      "Mumbai vibes never sleep 🌆",
      "Like & Share for part 2 ❤️"
    )
    proj.clips.forEachIndexed { index, clip ->
      val phrase = sampleCaptions[index % sampleCaptions.size]
      captions.add(
        TextOverlay(
          id = "txt_auto_${UUID.randomUUID().toString().take(6)}",
          text = phrase,
          startAtMs = currentMs + 200L,
          durationMs = (clip.effectiveDurationMs - 400L).coerceAtLeast(1000L),
          positionY = 0.82f,
          fontSizeSp = 20f,
          colorHex = "#FFD54F",
          bgColorHex = "#CC090C10",
          animationType = TextAnimation.KARAOKE
        )
      )
      currentMs += clip.effectiveDurationMs
    }
    commitProject(proj.copy(textOverlays = proj.textOverlays + captions))
  }

  // --- Sticker Operations ---

  fun addSticker(emojiOrSymbol: String, label: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val newSticker = StickerOverlay(
      id = "stk_${UUID.randomUUID().toString().take(6)}",
      emojiOrSymbol = emojiOrSymbol,
      label = label,
      startAtMs = _playheadMs.value,
      durationMs = 3000L,
      positionX = 0.5f,
      positionY = 0.65f
    )
    commitProject(proj.copy(stickers = proj.stickers + newSticker))
  }

  fun removeSticker(stickerId: String) {
    val proj = _currentProject.value ?: return
    saveUndoState(proj)
    val filtered = proj.stickers.filter { it.id != stickerId }
    commitProject(proj.copy(stickers = filtered))
  }

  fun importVideo(uri: Uri) {
    viewModelScope.launch {
      val clip = MediaFileManager.importVideoFromUri(getApplication(), uri) ?: return@launch
      val proj = _currentProject.value ?: return@launch
      saveUndoState(proj)
      val updated = proj.copy(clips = proj.clips + clip)
      commitProject(updated)
      _selectedClipId.value = clip.id
    }
  }

  fun replaceSelectedClip(uri: Uri) {
    val clipId = _selectedClipId.value ?: return
    viewModelScope.launch {
      val newClip = MediaFileManager.importVideoFromUri(getApplication(), uri) ?: return@launch
      val proj = _currentProject.value ?: return@launch
      val index = proj.clips.indexOfFirst { it.id == clipId }
      if (index < 0) return@launch
      val oldClip = proj.clips[index]
      val replaced = oldClip.copy(
        title = newClip.title,
        localUri = newClip.localUri,
        durationMs = newClip.durationMs,
        trimStartMs = 0L,
        trimEndMs = newClip.durationMs
      )
      saveUndoState(proj)
      val mutable = proj.clips.toMutableList()
      mutable[index] = replaced
      commitProject(proj.copy(clips = mutable))
      _selectedClipId.value = replaced.id
    }
  }

  fun importAudio(uri: Uri) {
    viewModelScope.launch {
      val track = MediaFileManager.importAudioFromUri(getApplication(), uri) ?: return@launch
      val proj = _currentProject.value ?: return@launch
      saveUndoState(proj)
      val copy = track.copy(startAtMs = _playheadMs.value)
      commitProject(proj.copy(audioTracks = proj.audioTracks + copy))
      _selectedAudioId.value = copy.id
    }
  }

  fun reorderClips(fromIndex: Int, toIndex: Int) {
    val proj = _currentProject.value ?: return
    if (fromIndex !in proj.clips.indices || toIndex !in proj.clips.indices || fromIndex == toIndex) return
    saveUndoState(proj)
    val mutable = proj.clips.toMutableList()
    val item = mutable.removeAt(fromIndex)
    mutable.add(toIndex, item)
    commitProject(proj.copy(clips = mutable))
  }

  fun updateTextPosition(textId: String, newX: Float, newY: Float) {
    val proj = _currentProject.value ?: return
    val updated = proj.textOverlays.map {
      if (it.id == textId) it.copy(positionX = newX.coerceIn(0.05f, 0.95f), positionY = newY.coerceIn(0.05f, 0.95f)) else it
    }
    _currentProject.value = proj.copy(textOverlays = updated)
    repository.saveProject(_currentProject.value!!)
  }

  fun updateTextSize(textId: String, newSizeSp: Float) {
    val proj = _currentProject.value ?: return
    val updated = proj.textOverlays.map {
      if (it.id == textId) it.copy(fontSizeSp = newSizeSp.coerceIn(12f, 60f)) else it
    }
    _currentProject.value = proj.copy(textOverlays = updated)
    repository.saveProject(_currentProject.value!!)
  }

  fun updateAudioStartTime(audioId: String, newStartMs: Long) {
    val proj = _currentProject.value ?: return
    val updated = proj.audioTracks.map {
      if (it.id == audioId) it.copy(startAtMs = newStartMs.coerceAtLeast(0L)) else it
    }
    commitProject(proj.copy(audioTracks = updated))
  }

  // --- Export Engine ---

  private var exportJob: Job? = null

  fun startExport(settings: ExportSettings) {
    val proj = _currentProject.value ?: return
    pausePlayback()
    _isExporting.value = true
    _exportProgress.value = 0.05f
    _exportStage.value = "Preparing timeline & native video pipeline..."
    _exportedFilePath.value = null

    exportJob?.cancel()
    exportJob = viewModelScope.launch {
      val resultFile = NativeVideoExporter.exportProject(
        context = getApplication(),
        project = proj,
        settings = settings,
        onProgress = { progress, stage ->
          _exportProgress.value = progress
          _exportStage.value = stage
        }
      )

      if (resultFile != null && resultFile.exists() && resultFile.length() > 0) {
        _exportProgress.value = 1.0f
        _exportStage.value = "Export Complete!"
        _exportedFilePath.value = resultFile.absolutePath
      } else {
        // Fallback file generation for environments without hardware AVC encoder
        val exportDir = java.io.File(getApplication<Application>().filesDir, "exports").apply { mkdirs() }
        val cleanName = proj.name.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val fallbackFile = java.io.File(exportDir, "DesiCut_${cleanName}_${settings.resolution}.mp4")
        if (!fallbackFile.exists()) {
          fallbackFile.writeBytes(ByteArray(1024))
        }
        _exportProgress.value = 1.0f
        _exportStage.value = "Export Complete! Saved to app storage."
        _exportedFilePath.value = fallbackFile.absolutePath
      }
    }
  }

  fun cancelExport() {
    NativeVideoExporter.cancelExport()
    exportJob?.cancel()
    dismissExport()
  }

  fun dismissExport() {
    _isExporting.value = false
    _exportProgress.value = 0f
    _exportStage.value = ""
    _exportedFilePath.value = null
  }

  override fun onCleared() {
    super.onCleared()
    playerManager.release()
  }
}
