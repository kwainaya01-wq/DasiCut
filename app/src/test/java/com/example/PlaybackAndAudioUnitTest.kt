package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.media.NativeVideoPlayerManager
import com.example.model.*
import com.example.ui.viewmodel.EditorViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaybackAndAudioUnitTest {

  private lateinit var context: Application
  private lateinit var viewModel: EditorViewModel

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    viewModel = EditorViewModel(context)
  }

  @Test
  fun testVideoClipVolumeAndEffectiveDuration() {
    // Normal 1.0x speed, 100% volume
    val clip1 = VideoClip(
      id = "clip_1",
      title = "Clip 1",
      colorStartHex = "#00B09B",
      colorEndHex = "#004D40",
      durationMs = 6000L,
      trimStartMs = 1000L,
      trimEndMs = 5000L,
      speed = 1.0f,
      volume = 1.0f,
      isMuted = false
    )
    assertEquals(4000L, clip1.effectiveDurationMs)
    assertEquals(1.0f, clip1.volume, 0.01f)
    assertFalse(clip1.isMuted)

    // 2.0x speed: duration should be halved to 2000ms
    val clipFast = clip1.copy(speed = 2.0f)
    assertEquals(2000L, clipFast.effectiveDurationMs)

    // 0.5x slow-mo: duration should be doubled to 8000ms
    val clipSlow = clip1.copy(speed = 0.5f)
    assertEquals(8000L, clipSlow.effectiveDurationMs)

    // 200% volume boost
    val clipBoosted = clip1.copy(volume = 2.0f)
    assertEquals(2.0f, clipBoosted.volume, 0.01f)

    // Muted clip
    val clipMuted = clip1.copy(isMuted = true)
    assertTrue(clipMuted.isMuted)
  }

  @Test
  fun testVolumeUpdateAndMuteControlsInViewModel() {
    val project = Project(
      id = "proj_test",
      name = "Volume Test Project",
      aspectRatio = AspectRatio.RATIO_9_16,
      clips = listOf(
        VideoClip(
          id = "clip_vol_1",
          title = "Clip Vol 1",
          colorStartHex = "#00B09B",
          colorEndHex = "#004D40",
          durationMs = 5000L,
          trimEndMs = 5000L,
          volume = 1.0f,
          isMuted = false
        )
      )
    )
    viewModel.openProject(project)

    // Test updating volume to 1.5x (150%)
    viewModel.updateClipVolume("clip_vol_1", 1.5f, false)
    var updatedClip = viewModel.currentProject.value?.clips?.find { it.id == "clip_vol_1" }
    assertNotNull(updatedClip)
    assertEquals(1.5f, updatedClip!!.volume, 0.01f)
    assertFalse(updatedClip.isMuted)

    // Test updating volume to 2.0x (200% max loudness boost)
    viewModel.updateClipVolume("clip_vol_1", 2.0f, false)
    updatedClip = viewModel.currentProject.value?.clips?.find { it.id == "clip_vol_1" }
    assertEquals(2.0f, updatedClip!!.volume, 0.01f)

    // Test muting
    viewModel.updateClipVolume("clip_vol_1", 2.0f, true)
    updatedClip = viewModel.currentProject.value?.clips?.find { it.id == "clip_vol_1" }
    assertTrue(updatedClip!!.isMuted)

    // Test toggleClipMute helper
    viewModel.toggleClipMute("clip_vol_1")
    updatedClip = viewModel.currentProject.value?.clips?.find { it.id == "clip_vol_1" }
    assertFalse(updatedClip!!.isMuted)
  }

  @Test
  fun testSplitPreservesOriginalAudioVolumeAndTrimBounds() {
    val project = Project(
      id = "proj_split_test",
      name = "Split Audio Test",
      aspectRatio = AspectRatio.RATIO_16_9,
      clips = listOf(
        VideoClip(
          id = "clip_split_target",
          title = "Main Clip",
          colorStartHex = "#00B09B",
          colorEndHex = "#004D40",
          durationMs = 8000L,
          trimStartMs = 1000L,
          trimEndMs = 7000L,
          speed = 1.0f,
          volume = 1.8f,
          isMuted = false,
          localUri = "/data/user/0/com.example/files/videos/test.mp4"
        )
      )
    )
    viewModel.openProject(project)
    viewModel.selectClip("clip_split_target")

    // Playhead at 3000ms (offset in clip = 3000ms; raw split point = 1000 + 3000 = 4000ms)
    viewModel.setPlayheadMs(3000L)
    viewModel.splitSelectedClipAtPlayhead()

    val currentClips = viewModel.currentProject.value?.clips ?: emptyList()
    assertEquals(2, currentClips.size)

    val part1 = currentClips[0]
    val part2 = currentClips[1]

    // Both parts must retain the original media file URI
    assertEquals("/data/user/0/com.example/files/videos/test.mp4", part1.localUri)
    assertEquals("/data/user/0/com.example/files/videos/test.mp4", part2.localUri)

    // Both parts must preserve the volume and unmuted state
    assertEquals(1.8f, part1.volume, 0.01f)
    assertEquals(1.8f, part2.volume, 0.01f)
    assertFalse(part1.isMuted)
    assertFalse(part2.isMuted)

    // Trim bounds must be properly partitioned: [1000, 4000] and [4000, 7000]
    assertEquals(1000L, part1.trimStartMs)
    assertEquals(4000L, part1.trimEndMs)
    assertEquals(3000L, part1.effectiveDurationMs)

    assertEquals(4000L, part2.trimStartMs)
    assertEquals(7000L, part2.trimEndMs)
    assertEquals(3000L, part2.effectiveDurationMs)
  }

  @Test
  fun testNativeVideoPlayerManagerAudioSettings() {
    val playerManager = NativeVideoPlayerManager(context)

    // Set normal volume
    playerManager.setClipAudioAndSpeed(volume = 1.0f, muted = false, speed = 1.0f)

    // Set 200% volume with boost
    playerManager.setClipAudioAndSpeed(volume = 2.0f, muted = false, speed = 1.5f)

    // Set mute
    playerManager.setClipAudioAndSpeed(volume = 2.0f, muted = true, speed = 1.0f)

    // Verify pause & release do not throw
    playerManager.pauseVideo()
    playerManager.release()
  }

  @Test
  fun testMultiClipTimelineSynchronizationWithPortraitAndLandscape() {
    // Create dummy video file to test actual local file path resolution
    val videoDir = File(context.filesDir, "videos").apply { mkdirs() }
    val dummyVideo = File(videoDir, "dummy_portrait_landscape.mp4").apply {
      FileOutputStream(this).use { it.write(ByteArray(512)) }
    }

    val clipPortrait = VideoClip(
      id = "clip_portrait",
      title = "Portrait 9:16",
      colorStartHex = "#FF5722",
      colorEndHex = "#E91E63",
      localUri = dummyVideo.absolutePath,
      durationMs = 5000L,
      trimStartMs = 500L,
      trimEndMs = 4500L,
      speed = 1.0f,
      volume = 1.2f
    )

    val clipLandscape = VideoClip(
      id = "clip_landscape",
      title = "Landscape 16:9",
      colorStartHex = "#2196F3",
      colorEndHex = "#00BCD4",
      localUri = dummyVideo.absolutePath,
      durationMs = 6000L,
      trimStartMs = 1000L,
      trimEndMs = 5000L,
      speed = 2.0f, // 2x speed -> effective 2000ms
      volume = 0.8f
    )

    val project = Project(
      id = "proj_multi",
      name = "Multi-Clip Multi-Format",
      aspectRatio = AspectRatio.RATIO_9_16,
      clips = listOf(clipPortrait, clipLandscape)
    )
    viewModel.openProject(project)

    // Total duration = 4000ms + 2000ms = 6000ms
    assertEquals(6000L, project.totalDurationMs)

    // Test timeline sync at playhead 2000ms (in clipPortrait)
    viewModel.setPlayheadMs(2000L)
    assertEquals(2000L, viewModel.playheadMs.value)

    // Test timeline sync at playhead 5000ms (1000ms into clipLandscape)
    viewModel.setPlayheadMs(5000L)
    assertEquals(5000L, viewModel.playheadMs.value)

    // Clean up
    dummyVideo.delete()
  }
}
