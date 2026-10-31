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
  DISSOLVE("Dissolve", "dissolve"),
  FADE_BLACK("Fade Black", "fade_black"),
  ZOOM_IN("Zoom In", "zoom_in"),
  GLITCH("Glitch", "glitch"),
  SLIDE_LEFT("Slide", "slide_left"),
  FLASH("White Flash", "flash"),
  WHIP_PAN("Whip Pan", "whip_pan")
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

enum class VideoEffect(val displayName: String, val description: String) {
  NONE("None", "No overlay effect"),
  GLITCH("Digital Glitch", "Cyber displacement and scan jumps"),
  RGB_SPLIT("RGB Split", "Prismatic chromatic fringe"),
  FILM_GRAIN("Film Grain", "Authentic 35mm motion dust"),
  VHS_SCANLINES("VHS Retro", "Vintage tape tracking lines"),
  VIGNETTE("Dark Vignette", "Soft dramatic corner falloff"),
  DREAMY_BLUR("Dream Bloom", "Glowing ethereal highlight diffusion"),
  MOTION_SHAKE("Beat Shake", "Dynamic rhythmic camera pulse")
}

enum class TextAnimation(val displayName: String) {
  NONE("None"),
  POP("Pop In"),
  FADE("Fade In"),
  SLIDE_UP("Slide Up"),
  TYPEWRITER("Typewriter"),
  KARAOKE("Karaoke Glow")
}

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
  val fadeInMs: Long = 0L,
  val fadeOutMs: Long = 0L,
  val isFrozen: Boolean = false,
  val captionPrompt: String? = null
) {
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
  val animationType: TextAnimation = TextAnimation.POP
)

data class StickerOverlay(
  val id: String,
  val emojiOrSymbol: String,
  val label: String,
  val startAtMs: Long = 0L,
  val durationMs: Long = 3000L,
  val positionX: Float = 0.5f,
  val positionY: Float = 0.75f,
  val scale: Float = 1.2f
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
      return maxOf(clipsDuration, audioDuration, textDuration, 1000L)
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
