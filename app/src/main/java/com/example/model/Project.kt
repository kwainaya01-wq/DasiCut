package com.example.model

enum class AspectRatio(
  val label: String,
  val subtitle: String,
  val ratio: Float,
  val defaultWidth: Int,
  val defaultHeight: Int
) {
  RATIO_9_16("9:16", "Reels / Shorts", 9f / 16f, 1080, 1920),
  RATIO_16_9("16:9", "YouTube / TV", 16f / 9f, 1920, 1080),
  RATIO_1_1("1:1", "Instagram Post", 1f, 1080, 1080),
  RATIO_4_5("4:5", "Portrait Feed", 4f / 5f, 1080, 1350),
  RATIO_21_9("21:9", "Cinematic", 21f / 9f, 2560, 1080)
}

enum class TransitionType(val displayName: String, val iconResName: String) {
  NONE("None", "none"),
  FADE_IN("Fade In", "fade_in"),
  FADE_OUT("Fade Out", "fade_out"),
  CROSS_DISSOLVE("Cross Dissolve", "cross_dissolve"),
  BLACK_FADE("Black Fade", "black_fade"),
  WHITE_FADE("White Fade", "white_fade"),
  ZOOM_IN("Zoom In", "zoom_in"),
  ZOOM_OUT("Zoom Out", "zoom_out"),
  SLIDE_LEFT("Slide Left", "slide_left"),
  SLIDE_RIGHT("Slide Right", "slide_right"),
  SLIDE_UP("Slide Up", "slide_up"),
  SLIDE_DOWN("Slide Down", "slide_down"),
  SWIPE_LEFT("Swipe Left", "swipe_left"),
  SWIPE_RIGHT("Swipe Right", "swipe_right"),
  CIRCLE_REVEAL("Circle Reveal", "circle_reveal"),
  BLUR_TRANSITION("Blur Transition", "blur_transition"),
  SPIN_TRANSITION("Spin Transition", "spin_transition"),
  FLASH_TRANSITION("Flash Transition", "flash_transition"),
  GLITCH_TRANSITION("Glitch Transition", "glitch_transition"),
  WIPE_TRANSITION("Wipe Transition", "wipe_transition"),
  RGB_SPLIT_TRANSITION("RGB Split Transition", "rgb_split_transition"),

  // Backward-compatibility aliases
  DISSOLVE("Cross Dissolve", "cross_dissolve"),
  FADE_BLACK("Black Fade", "black_fade"),
  FLASH("Flash Transition", "flash_transition"),
  GLITCH("Glitch Transition", "glitch_transition"),
  WHIP_PAN("Spin Transition", "spin_transition");

  companion object {
    val primaryTransitions: List<TransitionType> = listOf(
      NONE,
      FADE_IN,
      FADE_OUT,
      CROSS_DISSOLVE,
      BLACK_FADE,
      WHITE_FADE,
      ZOOM_IN,
      ZOOM_OUT,
      SLIDE_LEFT,
      SLIDE_RIGHT,
      SLIDE_UP,
      SLIDE_DOWN,
      SWIPE_LEFT,
      SWIPE_RIGHT,
      CIRCLE_REVEAL,
      BLUR_TRANSITION,
      SPIN_TRANSITION,
      FLASH_TRANSITION,
      GLITCH_TRANSITION,
      WIPE_TRANSITION,
      RGB_SPLIT_TRANSITION
    )
  }
}

enum class VideoFilter(val displayName: String, val description: String) {
  ORIGINAL("Original", "Natural unfiltered look"),
  WARM_DESI("Desi Golden", "Warm Indian sunlight tones"),
  BOLLYWOOD("Bollywood Teal", "Signature cinematic teal & orange"),
  COOL_TEAL("Teal Vibe", "Crisp cool cyan shadows"),
  VINTAGE_90S("90s Retro", "Nostalgic golden tape wash"),
  MOODY_NOIR("Moody Noir", "Deep high-contrast monochrome"),
  EMERALD_DREAM("Emerald Dream", "Lush rich green styling"),
  CYBER_NEON("Cyberpunk", "Vibrant futuristic magenta & blue"),
  SUNSET_CHAI("Sunset Chai", "Rich amber and crimson dusk"),
  MONOCHROME("Film B&W", "Silky analog black & white film")
}

enum class EffectCategory(val displayName: String) {
  ALL("All"),
  CINEMATIC("Cinematic"),
  GLITCH("Glitch"),
  LIGHT("Light"),
  RETRO("Retro"),
  BLUR("Blur"),
  CREATIVE("Creative"),
  COLOR("Color"),
  BODY("Body & Motion"),
  SHAKE("Shake & Camera"),
  PARTICLES("Particles"),
  DISTORTION("Distortion")
}

enum class VideoEffect(
  val displayName: String,
  val description: String,
  val category: EffectCategory = EffectCategory.CINEMATIC,
  val isPro: Boolean = false
) {
  NONE("None", "No overlay effect", EffectCategory.ALL, false),

  // Cinematic (8 effects)
  CINEMATIC_GLOW("Cinematic Glow", "Soft highlight bloom and dreamy warmth", EffectCategory.CINEMATIC, false),
  FILM_LOOK("Film Look", "Deep analog shadows and organic film response", EffectCategory.CINEMATIC, false),
  TEAL_AND_ORANGE("Teal & Orange", "Hollywood blockbuster dual-tone color grading", EffectCategory.CINEMATIC, true),
  MOVIE_GRAIN("Movie Grain", "Authentic 35mm motion dust and silver halide grain", EffectCategory.CINEMATIC, false),
  SOFT_FOCUS("Soft Focus", "Delicate portrait diffusion and skin smoothing", EffectCategory.CINEMATIC, false),
  CINEMATIC_BLUR("Cinematic Blur", "Anamorphic shallow depth-of-field edge blur", EffectCategory.CINEMATIC, true),
  VIGNETTE("Dark Vignette", "Soft dramatic corner exposure falloff", EffectCategory.CINEMATIC, false),
  BLEACH_BYPASS("Bleach Bypass", "High-contrast silver retention desaturated film look", EffectCategory.CINEMATIC, true),

  // Glitch (8 effects)
  GLITCH("Digital Glitch", "Cyber displacement and horizontal scan jumps", EffectCategory.GLITCH, false),
  RGB_SPLIT("RGB Split", "Prismatic chromatic fringe and channel divergence", EffectCategory.GLITCH, false),
  VHS_GLITCH("VHS Glitch", "Magnetic tape distortion and horizontal tear", EffectCategory.GLITCH, false),
  SIGNAL_DISTORTION("Signal Distortion", "Analog TV antenna static and RF interference", EffectCategory.GLITCH, true),
  PIXEL_GLITCH("Pixel Glitch", "Blocky 8-bit digital pixel displacement", EffectCategory.GLITCH, false),
  CHROMATIC_ABERRATION("Chromatic Aberration", "Radial spectral color fringe at perimeter", EffectCategory.GLITCH, true),
  DATA_MOSH("Data Mosh", "Compression artifact frame bleed and pixel smear", EffectCategory.GLITCH, true),
  SCANLINE_WAVE("Scanline Wave", "Rolling CRT electron beam bands and phosphor ripple", EffectCategory.GLITCH, false),

  // Light (8 effects)
  LENS_FLARE("Lens Flare", "Golden optical aperture flare and rings", EffectCategory.LIGHT, false),
  LIGHT_LEAK("Light Leak", "Warm analog vintage camera light leak", EffectCategory.LIGHT, false),
  FLASH("Flash", "High-energy rhythmic strobe pulse and blinding bursts", EffectCategory.LIGHT, false),
  GLOW("Glow", "Ethereal luminous halo around highlights", EffectCategory.LIGHT, false),
  NEON_GLOW("Neon Glow", "Vibrant cyber pink and electric blue aura", EffectCategory.LIGHT, true),
  GOLDEN_LIGHT("Golden Light", "Rich sunset ray infusion and radiant warm wash", EffectCategory.LIGHT, false),
  PRISM_RAINBOW("Prism Rainbow", "Refracted crystal spectrum scatter and rainbow shards", EffectCategory.LIGHT, true),
  SUN_RAYS("Sun Rays", "Volumetric crepuscular light beams streaming from above", EffectCategory.LIGHT, true),

  // Retro (8 effects)
  VINTAGE("Vintage", "1970s warm fade, yellowed paper and gentle vignette", EffectCategory.RETRO, false),
  VHS_SCANLINES("VHS Retro", "Vintage tape tracking lines and phosphor buzz", EffectCategory.RETRO, false),
  RETRO_FILM("Retro Film", "Aged 16mm celluloid with border burn and flicker", EffectCategory.RETRO, false),
  LOOK_90S("90s Look", "Cool contrasty home camcorder aesthetics", EffectCategory.RETRO, false),
  OLD_CAMERA("Old Camera", "Sepia-tinged daguerreotype texture and historic patina", EffectCategory.RETRO, true),
  FILM_BURN("Film Burn", "Organic orange celluloid burn and edge heat flare", EffectCategory.RETRO, true),
  SILENT_8MM("8mm Silent", "Flickering monochrome classic reel with projector shudder", EffectCategory.RETRO, true),
  DISCO_70S("70s Disco", "Psychedelic saturated warmth and star filter glints", EffectCategory.RETRO, true),

  // Blur (7 effects)
  GAUSSIAN_BLUR("Gaussian Blur", "Smooth multi-directional atmospheric softness", EffectCategory.BLUR, false),
  MOTION_BLUR("Motion Blur", "High-speed directional velocity streaks", EffectCategory.BLUR, false),
  RADIAL_BLUR("Radial Blur", "Rotational vortex blur emanating from center", EffectCategory.BLUR, true),
  ZOOM_BLUR("Zoom Blur", "Explosive focal zoom blur with speed trails", EffectCategory.BLUR, true),
  BACKGROUND_BLUR("Background Blur", "Dramatic bokeh vignette blur keeping center crisp", EffectCategory.BLUR, false),
  TILT_SHIFT("Tilt Shift", "Miniature diorama horizontal focus slice", EffectCategory.BLUR, true),
  DREAMY_BLUR("Dream Bloom", "Glowing ethereal highlight diffusion and bloom", EffectCategory.BLUR, false),

  // Creative (8 effects)
  BLACK_AND_WHITE("Black & White", "Silky monochrome fine art tone with deep blacks", EffectCategory.CREATIVE, false),
  NEGATIVE("Negative", "Inverted color photographic film negative", EffectCategory.CREATIVE, false),
  SKETCH("Sketch", "Pencil cross-hatch contour and charcoal outline", EffectCategory.CREATIVE, true),
  CARTOON_STYLE("Cartoon Style", "Posterized pop-art comic illustration and ink edges", EffectCategory.CREATIVE, true),
  MIRROR("Mirror", "Bilateral symmetrical reflective mirror illusion", EffectCategory.CREATIVE, false),
  KALEIDOSCOPE("Kaleidoscope", "Geometric multifaceted crystal symmetry pattern", EffectCategory.CREATIVE, true),
  THERMAL_VISION("Thermal Vision", "Infrared heat-map false color spectrum", EffectCategory.CREATIVE, true),
  HALFTONE("Halftone", "Vintage printed newspaper dot matrix screen", EffectCategory.CREATIVE, true),

  // Color (10 effects)
  WARM("Warm", "Cozy golden sun and amber boost", EffectCategory.COLOR, false),
  COOL("Cool", "Refreshing arctic cyan and blue tones", EffectCategory.COLOR, false),
  FADE("Fade", "Lifted matte blacks and pastel mist", EffectCategory.COLOR, false),
  SATURATION_BOOST("Saturation Boost", "Vivid explosive color pop and deep vibrance", EffectCategory.COLOR, false),
  HIGH_CONTRAST("High Contrast", "Punchy deep shadows and bright punchy highlights", EffectCategory.COLOR, false),
  SEPIA("Sepia", "Nostalgic antique brown monochrome wash", EffectCategory.COLOR, false),
  NIGHT_VISION("Night Vision", "Military phosphor green tactical surveillance", EffectCategory.COLOR, true),
  HDR_LOOK("HDR Look", "Ultra dynamic clarity, local contrast and shadow recovery", EffectCategory.COLOR, true),
  SUNSET_AMBER("Sunset Amber", "Lush crimson evening glow and dusk warmth", EffectCategory.COLOR, false),
  EMERALD_VIVID("Vivid Emerald", "Signature DesiCut rich emerald jewel tint", EffectCategory.COLOR, false),

  // Shake & Camera Motion (Part 3)
  CAMERA_SHAKE("Camera Shake", "Handheld organic camera tremor and jitter", EffectCategory.SHAKE, false),
  BEAT_SHAKE("Beat Shake", "High-impact rhythmic bass drop shake", EffectCategory.SHAKE, false),
  EARTHQUAKE("Earthquake Shake", "Violent dual-axis rumbling tremor", EffectCategory.SHAKE, true),
  WHIP_ZOOM("Whip Zoom Shake", "Rapid punch-in bounce and recovery", EffectCategory.SHAKE, true),

  // Body & Face Effects (Part 3)
  BODY_NEON_STROKE("Body Neon Stroke", "Luminous cyber silhouette edge tracing", EffectCategory.BODY, true),
  AURA_GLOW("Aura Glow", "Radiant golden and electric aura envelope", EffectCategory.BODY, false),
  SHADOW_CLONE("Shadow Clone", "Trailing echo silhouette after-images", EffectCategory.BODY, true),
  PORTRAIT_FOCUS("Portrait Focus", "Subject pop with background separation", EffectCategory.BODY, false),

  // Particles (Part 3)
  GOLD_DUST_PARTICLES("Gold Dust", "Drifting luminous golden sparkles and embers", EffectCategory.PARTICLES, false),
  CONFETTI_BURST("Confetti Burst", "Festive Bollywood party celebration confetti", EffectCategory.PARTICLES, false),
  SNOW_FALL("Snow Fall", "Delicate atmospheric winter snow particles", EffectCategory.PARTICLES, false),
  FIRE_EMBERS("Fire Embers", "Rising glowing volcanic heat embers", EffectCategory.PARTICLES, true),

  // Distortion (Part 3)
  WARP_DISTORTION("Warp Wave", "Liquid ripple wave warp distortion", EffectCategory.DISTORTION, true),
  FISHEYE_LENS("Fisheye Lens", "Ultra-wide optical barrel distortion", EffectCategory.DISTORTION, false),
  VORTEX_SWIRL("Vortex Swirl", "Hypnotic spiral whirlpool displacement", EffectCategory.DISTORTION, true),
  RIPPLE_DISPLACEMENT("Water Ripple", "Pond concentric acoustic wave ripple", EffectCategory.DISTORTION, false),

  // Aliases
  FILM_GRAIN("Film Grain", "Authentic 35mm motion dust and grain", EffectCategory.CINEMATIC, false),
  MOTION_SHAKE("Motion Shake", "Dynamic rhythmic camera pulse and vibration", EffectCategory.SHAKE, false)
}

data class AppliedEffect(
  val id: String = java.util.UUID.randomUUID().toString().take(6),
  val effect: VideoEffect,
  val intensity: Float = 0.8f
)

enum class TextAnimation(val displayName: String) {
  NONE("None"),
  POP("Pop In"),
  FADE("Fade In"),
  SLIDE_UP("Slide Up"),
  TYPEWRITER("Typewriter"),
  KARAOKE("Karaoke Glow")
}

data class FaceRetouchSettings(
  val isEnabled: Boolean = false,
  val overallIntensity: Float = 0.70f,   // 0f..1f (0-100%)
  val skinSmoothing: Float = 0.65f,      // 0f..1f
  val faceBrightness: Float = 0.50f,     // 0f..1f
  val blemishReduction: Float = 0.55f,   // 0f..1f
  val eyeEnhancement: Float = 0.50f,     // 0f..1f
  val naturalSkinTone: Float = 0.60f     // 0f..1f
)

data class VideoClip(
  val id: String,
  val title: String,
  val colorStartHex: String,
  val colorEndHex: String,
  val localUri: String? = null,
  val durationMs: Long = 4000L,
  val trimStartMs: Long = 0L,
  val trimEndMs: Long = 4000L,
  val speed: Float = 1.0f,
  val volume: Float = 1.0f,
  val isMuted: Boolean = false,
  val rotationDegrees: Int = 0,
  val isFlippedH: Boolean = false,
  val isFlippedV: Boolean = false,
  val isReversed: Boolean = false,
  val transitionToNext: TransitionType = TransitionType.NONE,
  val transitionDurationMs: Long = 500L,
  val filter: VideoFilter = VideoFilter.ORIGINAL,
  val filterIntensity: Float = 1.0f,
  val effect: VideoEffect = VideoEffect.NONE,
  val effectIntensity: Float = 0.8f,
  val effects: List<AppliedEffect> = emptyList(),
  val retouch: FaceRetouchSettings = FaceRetouchSettings(),
  val fadeInMs: Long = 0L,
  val fadeOutMs: Long = 0L,
  val isFrozen: Boolean = false,
  val captionPrompt: String? = null
) {
  val activeEffects: List<AppliedEffect>
    get() {
      if (effects.isNotEmpty()) return effects
      if (effect != VideoEffect.NONE) return listOf(AppliedEffect(id = "legacy_${id}", effect = effect, intensity = effectIntensity))
      return emptyList()
    }

  val effectiveDurationMs: Long
    get() {
      val raw = (trimEndMs - trimStartMs).coerceAtLeast(200L)
      return (raw / speed.coerceIn(0.2f, 5.0f)).toLong()
    }
}

data class AudioTrack(
  val id: String,
  val title: String,
  val artist: String,
  val category: String, // BGM, Sound FX, Voiceover
  val startAtMs: Long = 0L,
  val durationMs: Long = 15000L,
  val volume: Float = 1.0f,
  val isMuted: Boolean = false,
  val fadeInMs: Long = 0L,
  val fadeOutMs: Long = 0L,
  val waveformPoints: List<Float> = listOf(0.3f, 0.6f, 0.9f, 0.4f, 0.8f, 0.5f, 0.7f, 1.0f, 0.4f, 0.6f)
)

data class TextOverlay(
  val id: String,
  val text: String,
  val startAtMs: Long = 0L,
  val durationMs: Long = 3000L,
  val positionX: Float = 0.5f,
  val positionY: Float = 0.5f,
  val fontSizeSp: Float = 26f,
  val colorHex: String = "#FFFFFF",
  val bgColorHex: String? = "#80000000",
  val hasStroke: Boolean = true,
  val strokeColorHex: String = "#000000",
  val fontStyleName: String = "Outfit Bold",
  val animationType: TextAnimation = TextAnimation.POP,
  val rotationDegrees: Float = 0f,
  val scale: Float = 1.0f,
  val alignment: String = "CENTER", // LEFT, CENTER, RIGHT
  val hasShadow: Boolean = false,
  val shadowColorHex: String = "#000000"
)

data class StickerOverlay(
  val id: String,
  val emojiOrSymbol: String,
  val label: String,
  val startAtMs: Long = 0L,
  val durationMs: Long = 3000L,
  val positionX: Float = 0.5f,
  val positionY: Float = 0.75f,
  val scale: Float = 1.2f,
  val rotationDegrees: Float = 0f,
  val hasShadow: Boolean = false
)

enum class UserRole(val displayName: String) {
  USER("Creator"),
  OWNER("Studio Owner")
}

data class EffectPreset(
  val id: String = java.util.UUID.randomUUID().toString().take(8),
  val name: String,
  val description: String,
  val category: String = "Cinematic",
  val thumbnailIcon: String = "✨",
  val effects: List<AppliedEffect> = emptyList(),
  val isPublished: Boolean = true,
  val isDraft: Boolean = false,
  val createdBy: String = "Studio Owner",
  val authorizedParams: List<String> = listOf("intensity", "speed"),
  val tags: List<String> = emptyList(),
  val createdAt: Long = System.currentTimeMillis()
)

data class CustomTemplate(
  val id: String = "tpl_${java.util.UUID.randomUUID().toString().take(8)}",
  val title: String,
  val category: String = "Trending",
  val tagline: String = "Created in Owner Studio",
  val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
  val durationMs: Long = 8000L,
  val requiredClipsCount: Int = 3,
  val bgmName: String = "Desi Beats",
  val bpm: Int = 128,
  val usesCount: String = "1.5k",
  val previewClips: List<VideoClip> = emptyList(),
  val previewTexts: List<TextOverlay> = emptyList(),
  val previewBgm: AudioTrack? = null,
  val gradientStartHex: String = "#0A2E20",
  val gradientEndHex: String = "#051610",
  val isPublished: Boolean = true,
  val isDraft: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

data class Project(
  val id: String,
  val name: String,
  val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
  val updatedAt: Long = System.currentTimeMillis(),
  val clips: List<VideoClip> = emptyList(),
  val audioTracks: List<AudioTrack> = emptyList(),
  val textOverlays: List<TextOverlay> = emptyList(),
  val stickers: List<StickerOverlay> = emptyList(),
  val quality: String = "1080p",
  val fps: Int = 30
) {
  val totalDurationMs: Long
    get() {
      val clipsDuration = clips.sumOf { it.effectiveDurationMs }
      val audioDuration = audioTracks.maxOfOrNull { it.startAtMs + it.durationMs } ?: 0L
      val textDuration = textOverlays.maxOfOrNull { it.startAtMs + it.durationMs } ?: 0L
      val stickerDuration = stickers.maxOfOrNull { it.startAtMs + it.durationMs } ?: 0L
      return maxOf(clipsDuration, audioDuration, textDuration, stickerDuration, 1000L)
    }
}

data class Template(
  val id: String,
  val title: String,
  val category: String,
  val tagline: String,
  val aspectRatio: AspectRatio,
  val durationMs: Long,
  val requiredClipsCount: Int,
  val bgmName: String,
  val bpm: Int,
  val usesCount: String,
  val previewClips: List<VideoClip>,
  val previewTexts: List<TextOverlay>,
  val previewBgm: AudioTrack,
  val gradientColors: Pair<String, String>
)

data class ExportSettings(
  val resolution: String = "1080p",
  val fps: Int = 30,
  val bitrateMbps: Float = 12.0f,
  val qualityLabel: String = "Full HD"
)
