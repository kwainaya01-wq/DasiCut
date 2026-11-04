package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class TransitionsAndEffectsUnitTest {

  @Test
  fun testAllTwentyTransitionsPresent() {
    val transitions = TransitionType.primaryTransitions
    // Must contain NONE plus 20 real video transitions = 21 items
    assertEquals(21, transitions.size)

    val required20 = listOf(
      TransitionType.FADE_IN,
      TransitionType.FADE_OUT,
      TransitionType.CROSS_DISSOLVE,
      TransitionType.BLACK_FADE,
      TransitionType.WHITE_FADE,
      TransitionType.ZOOM_IN,
      TransitionType.ZOOM_OUT,
      TransitionType.SLIDE_LEFT,
      TransitionType.SLIDE_RIGHT,
      TransitionType.SLIDE_UP,
      TransitionType.SLIDE_DOWN,
      TransitionType.SWIPE_LEFT,
      TransitionType.SWIPE_RIGHT,
      TransitionType.CIRCLE_REVEAL,
      TransitionType.BLUR_TRANSITION,
      TransitionType.SPIN_TRANSITION,
      TransitionType.FLASH_TRANSITION,
      TransitionType.GLITCH_TRANSITION,
      TransitionType.WIPE_TRANSITION,
      TransitionType.RGB_SPLIT_TRANSITION
    )

    for (t in required20) {
      assertTrue("Missing required transition: ${t.displayName}", transitions.contains(t))
      assertFalse("Display name must not be blank", t.displayName.isBlank())
    }
  }

  @Test
  fun testFiftyPlusVideoEffectsAcrossAllCategories() {
    val nonNoneEffects = VideoEffect.values().filter { it != VideoEffect.NONE }
    assertTrue("Effects library must have at least 50 effects (found ${nonNoneEffects.size})", nonNoneEffects.size >= 50)

    val categories = EffectCategory.values().filter { it != EffectCategory.ALL }
    assertTrue("Must have at least 7 defined effect categories", categories.size >= 7)

    // Check each category has effects
    for (cat in categories) {
      val catEffects = nonNoneEffects.filter { it.category == cat }
      assertTrue("Category ${cat.displayName} must have effects", catEffects.isNotEmpty())
    }

    // Verify PRO labels exist alongside free effects
    val proEffects = nonNoneEffects.filter { it.isPro }
    val freeEffects = nonNoneEffects.filter { !it.isPro }
    assertTrue("Must have PRO effects", proEffects.isNotEmpty())
    assertTrue("Must have free effects", freeEffects.isNotEmpty())

    // Check specific required effects from user prompt
    val names = nonNoneEffects.map { it.displayName }
    assertTrue(names.contains("Cinematic Glow"))
    assertTrue(names.contains("Film Look"))
    assertTrue(names.contains("Teal & Orange"))
    assertTrue(names.contains("Movie Grain") || names.contains("Film Grain"))
    assertTrue(names.contains("Soft Focus"))
    assertTrue(names.contains("Cinematic Blur"))
    assertTrue(names.contains("Digital Glitch"))
    assertTrue(names.contains("RGB Split"))
    assertTrue(names.contains("Lens Flare"))
    assertTrue(names.contains("Light Leak"))
    assertTrue(names.contains("Vintage"))
    assertTrue(names.contains("VHS Retro"))
    assertTrue(names.contains("Gaussian Blur"))
    assertTrue(names.contains("Motion Blur"))
    assertTrue(names.contains("Black & White"))
    assertTrue(names.contains("Negative"))
    assertTrue(names.contains("Sketch"))
    assertTrue(names.contains("Cartoon Style"))
    assertTrue(names.contains("Mirror"))
    assertTrue(names.contains("Kaleidoscope"))
    assertTrue(names.contains("Warm"))
    assertTrue(names.contains("Cool"))
    assertTrue(names.contains("Fade"))
    assertTrue(names.contains("Saturation Boost"))
    assertTrue(names.contains("High Contrast"))
    assertTrue(names.contains("Sepia"))
    assertTrue(names.contains("Night Vision"))
    assertTrue(names.contains("HDR Look"))
  }

  @Test
  fun testMultiEffectStackOnVideoClip() {
    val clip = VideoClip(
      id = "test_clip",
      title = "Test Clip",
      colorStartHex = "#FF512F",
      colorEndHex = "#DD2476",
      effects = listOf(
        AppliedEffect(id = "fx1", effect = VideoEffect.CINEMATIC_GLOW, intensity = 0.9f),
        AppliedEffect(id = "fx2", effect = VideoEffect.MOVIE_GRAIN, intensity = 0.4f),
        AppliedEffect(id = "fx3", effect = VideoEffect.TEAL_AND_ORANGE, intensity = 0.75f)
      )
    )

    assertEquals(3, clip.activeEffects.size)
    assertEquals(VideoEffect.CINEMATIC_GLOW, clip.activeEffects[0].effect)
    assertEquals(0.9f, clip.activeEffects[0].intensity, 0.01f)
    assertEquals(VideoEffect.MOVIE_GRAIN, clip.activeEffects[1].effect)
    assertEquals(VideoEffect.TEAL_AND_ORANGE, clip.activeEffects[2].effect)

    // Reorder test
    val reordered = clip.activeEffects.toMutableList().apply {
      val item = removeAt(0)
      add(2, item)
    }
    assertEquals(VideoEffect.MOVIE_GRAIN, reordered[0].effect)
    assertEquals(VideoEffect.TEAL_AND_ORANGE, reordered[1].effect)
    assertEquals(VideoEffect.CINEMATIC_GLOW, reordered[2].effect)
  }

  @Test
  fun testTransitionDurationConstraint() {
    val clip = VideoClip(
      id = "clip_trans",
      title = "Transition Test",
      colorStartHex = "#000000",
      colorEndHex = "#FFFFFF",
      transitionToNext = TransitionType.CIRCLE_REVEAL,
      transitionDurationMs = 1200L
    )

    assertEquals(TransitionType.CIRCLE_REVEAL, clip.transitionToNext)
    assertEquals(1200L, clip.transitionDurationMs)
    assertTrue("Duration must be within 200..3000ms", clip.transitionDurationMs in 200L..3000L)
  }
}
