package com.example

import android.graphics.Bitmap
import com.example.media.FaceRetouchProcessor
import com.example.model.FaceRetouchSettings
import com.example.model.VideoClip
import com.example.model.VideoEffect
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FaceRetouchAndLayoutTest {

  @Test
  fun testFaceRetouchSettingsDefaults() {
    val settings = FaceRetouchSettings()
    assertFalse(settings.isEnabled)
    assertEquals(0.70f, settings.overallIntensity, 0.01f)
    assertEquals(0.65f, settings.skinSmoothing, 0.01f)
    assertEquals(0.50f, settings.faceBrightness, 0.01f)
    assertEquals(0.55f, settings.blemishReduction, 0.01f)
    assertEquals(0.50f, settings.eyeEnhancement, 0.01f)
    assertEquals(0.60f, settings.naturalSkinTone, 0.01f)
  }

  @Test
  fun testFaceRetouchSettingsCustomCopy() {
    val settings = FaceRetouchSettings(isEnabled = true, overallIntensity = 0.9f)
    assertTrue(settings.isEnabled)
    assertEquals(0.9f, settings.overallIntensity, 0.01f)

    val updated = settings.copy(skinSmoothing = 0.85f, faceBrightness = 0.6f)
    assertEquals(0.85f, updated.skinSmoothing, 0.01f)
    assertEquals(0.6f, updated.faceBrightness, 0.01f)
  }

  @Test
  fun testVideoClipRetouchIntegration() {
    val clip = VideoClip(
      id = "clip_test_1",
      title = "Test Clip",
      colorStartHex = "#000000",
      colorEndHex = "#FFFFFF",
      durationMs = 5000L,
      retouch = FaceRetouchSettings(isEnabled = true, skinSmoothing = 0.9f)
    )
    assertTrue(clip.retouch.isEnabled)
    assertEquals(0.9f, clip.retouch.skinSmoothing, 0.01f)
  }

  @Test
  fun testRetouchProcessorWithBitmap() {
    val testBitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
    val settings = FaceRetouchSettings(isEnabled = true, skinSmoothing = 0.8f)

    val retouched = FaceRetouchProcessor.applyRetouch(testBitmap, settings)
    assertNotNull(retouched)
    assertEquals(120, retouched.width)
    assertEquals(120, retouched.height)
  }

  @Test
  fun testDisabledRetouchReturnsUnmodified() {
    val testBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
    val disabledSettings = FaceRetouchSettings(isEnabled = false)

    val result = FaceRetouchProcessor.applyRetouch(testBitmap, disabledSettings)
    assertSame(testBitmap, result)
  }

  @Test
  fun testVideoEffectsLibraryCount() {
    val totalEffects = VideoEffect.values().filter { it != VideoEffect.NONE }
    // Must have at least 50 real video effects
    assertTrue("Effects library should contain 50+ effects, found ${totalEffects.size}", totalEffects.size >= 50)
  }
}
