package com.example.data

import com.example.model.*

object SampleData {

  val royaltyFreeAudio = listOf(
    AudioTrack(
      id = "audio_1",
      title = "Chai & Chill (Lo-Fi)",
      artist = "DesiCut Originals",
      category = "Lo-Fi",
      durationMs = 15000L,
      waveformPoints = listOf(0.2f, 0.4f, 0.7f, 0.5f, 0.8f, 0.9f, 0.6f, 0.4f, 0.7f, 0.5f, 0.8f, 0.3f)
    ),
    AudioTrack(
      id = "audio_2",
      title = "Bhangra Bass Boost (128 BPM)",
      artist = "DJ Dholak",
      category = "Bhangra",
      durationMs = 18000L,
      waveformPoints = listOf(0.8f, 0.9f, 1.0f, 0.85f, 0.95f, 0.7f, 1.0f, 0.9f, 0.8f, 1.0f, 0.7f, 0.9f)
    ),
    AudioTrack(
      id = "audio_3",
      title = "Acoustic Sitar Dreams",
      artist = "Raga Lounge",
      category = "Cinematic",
      durationMs = 20000L,
      waveformPoints = listOf(0.3f, 0.5f, 0.6f, 0.8f, 0.6f, 0.7f, 0.5f, 0.4f, 0.6f, 0.7f, 0.5f, 0.3f)
    ),
    AudioTrack(
      id = "audio_4",
      title = "Mumbai Night Drive",
      artist = "Synthwave India",
      category = "Electronic",
      durationMs = 16000L,
      waveformPoints = listOf(0.5f, 0.7f, 0.8f, 0.9f, 0.6f, 0.8f, 0.7f, 0.9f, 0.8f, 0.6f, 0.7f, 0.5f)
    ),
    AudioTrack(
      id = "audio_5",
      title = "Vlog Morning Vibes",
      artist = "Acoustic Strings",
      category = "Vlog",
      durationMs = 14000L,
      waveformPoints = listOf(0.4f, 0.6f, 0.5f, 0.7f, 0.6f, 0.8f, 0.5f, 0.7f, 0.6f, 0.4f, 0.5f, 0.3f)
    )
  )

  val soundEffects = listOf(
    AudioTrack(
      id = "sfx_1",
      title = "Whoosh Fast Transition",
      artist = "Sound FX",
      category = "Transition",
      durationMs = 800L,
      waveformPoints = listOf(0.2f, 0.6f, 1.0f, 0.7f, 0.1f)
    ),
    AudioTrack(
      id = "sfx_2",
      title = "Camera Shutter Click",
      artist = "Sound FX",
      category = "Foley",
      durationMs = 500L,
      waveformPoints = listOf(0.9f, 0.3f, 0.8f, 0.2f)
    ),
    AudioTrack(
      id = "sfx_3",
      title = "Sub Bass Impact Drop",
      artist = "Sound FX",
      category = "Impact",
      durationMs = 1500L,
      waveformPoints = listOf(1.0f, 0.8f, 0.6f, 0.4f, 0.2f)
    ),
    AudioTrack(
      id = "sfx_4",
      title = "Vinyl Tape Scratch",
      artist = "Sound FX",
      category = "Retro",
      durationMs = 700L,
      waveformPoints = listOf(0.4f, 0.8f, 0.7f, 0.9f, 0.2f)
    ),
    AudioTrack(
      id = "sfx_5",
      title = "Notification Chime",
      artist = "Sound FX",
      category = "UI",
      durationMs = 600L,
      waveformPoints = listOf(0.5f, 0.9f, 0.4f, 0.2f)
    )
  )

  val stickersList = listOf(
    StickerOverlay(id = "stk_1", emojiOrSymbol = "🇮🇳", label = "India Flag"),
    StickerOverlay(id = "stk_2", emojiOrSymbol = "🔥", label = "Fire Trend"),
    StickerOverlay(id = "stk_3", emojiOrSymbol = "🙏", label = "Namaste"),
    StickerOverlay(id = "stk_4", emojiOrSymbol = "✨", label = "Sparkles"),
    StickerOverlay(id = "stk_5", emojiOrSymbol = "☕", label = "Desi Chai"),
    StickerOverlay(id = "stk_6", emojiOrSymbol = "🎬", label = "Cinema"),
    StickerOverlay(id = "stk_7", emojiOrSymbol = "❤️", label = "Love"),
    StickerOverlay(id = "stk_8", emojiOrSymbol = "🎧", label = "Beats")
  )

  val templates = listOf(
    Template(
      id = "tpl_1",
      title = "Trending Beat Drop",
      category = "Trending",
      tagline = "Sync clips on every heavy 808 kick",
      aspectRatio = AspectRatio.RATIO_9_16,
      durationMs = 9500L,
      requiredClipsCount = 4,
      bgmName = "Bhangra Bass Boost",
      bpm = 128,
      usesCount = "480K",
      previewClips = listOf(
        VideoClip(id = "c1", title = "Intro Walk", colorStartHex = "#FF512F", colorEndHex = "#DD2476", durationMs = 2500L, trimEndMs = 2500L, transitionToNext = TransitionType.FLASH),
        VideoClip(id = "c2", title = "Fast Swag", colorStartHex = "#1D976C", colorEndHex = "#93F9B9", durationMs = 2000L, trimEndMs = 2000L, effect = VideoEffect.GLITCH, transitionToNext = TransitionType.ZOOM_IN),
        VideoClip(id = "c3", title = "Drop Moment", colorStartHex = "#8E2DE2", colorEndHex = "#4A00E0", durationMs = 2500L, trimEndMs = 2500L, effect = VideoEffect.RGB_SPLIT, transitionToNext = TransitionType.WHIP_PAN),
        VideoClip(id = "c4", title = "Outro Look", colorStartHex = "#00C9FF", colorEndHex = "#92FE9D", durationMs = 2500L, trimEndMs = 2500L, filter = VideoFilter.WARM_DESI)
      ),
      previewTexts = listOf(
        TextOverlay(id = "t1", text = "WAIT FOR THE DROP 🔥", startAtMs = 0L, durationMs = 2500L, positionY = 0.35f, colorHex = "#FFD54F")
      ),
      previewBgm = royaltyFreeAudio[1],
      gradientColors = Pair("#FF512F", "#DD2476")
    ),
    Template(
      id = "tpl_2",
      title = "Bollywood Cinematic",
      category = "Cinematic",
      tagline = "Slow-mo teal & orange aesthetic",
      aspectRatio = AspectRatio.RATIO_9_16,
      durationMs = 12000L,
      requiredClipsCount = 3,
      bgmName = "Acoustic Sitar Dreams",
      bpm = 85,
      usesCount = "310K",
      previewClips = listOf(
        VideoClip(id = "c1", title = "Dusk Sky", colorStartHex = "#2C3E50", colorEndHex = "#FD746C", durationMs = 4000L, trimEndMs = 4000L, speed = 0.7f, filter = VideoFilter.BOLLYWOOD, transitionToNext = TransitionType.DISSOLVE),
        VideoClip(id = "c2", title = "Slow Smile", colorStartHex = "#136A8A", colorEndHex = "#267871", durationMs = 4000L, trimEndMs = 4000L, speed = 0.5f, filter = VideoFilter.BOLLYWOOD, transitionToNext = TransitionType.FADE_BLACK),
        VideoClip(id = "c3", title = "Sunset Turn", colorStartHex = "#F3904F", colorEndHex = "#3B4371", durationMs = 4000L, trimEndMs = 4000L, speed = 0.6f, filter = VideoFilter.WARM_DESI)
      ),
      previewTexts = listOf(
        TextOverlay(id = "t1", text = "SUKOON ✨", startAtMs = 1000L, durationMs = 4000L, positionY = 0.6f, colorHex = "#FFFFFF")
      ),
      previewBgm = royaltyFreeAudio[2],
      gradientColors = Pair("#136A8A", "#267871")
    ),
    Template(
      id = "tpl_3",
      title = "Desi Food Vlog",
      category = "Reels",
      tagline = "Crisp edits for street food & recipes",
      aspectRatio = AspectRatio.RATIO_9_16,
      durationMs = 11000L,
      requiredClipsCount = 4,
      bgmName = "Chai & Chill (Lo-Fi)",
      bpm = 95,
      usesCount = "220K",
      previewClips = listOf(
        VideoClip(id = "c1", title = "Cutting Masala", colorStartHex = "#F857A6", colorEndHex = "#FF5858", durationMs = 3000L, trimEndMs = 3000L, filter = VideoFilter.WARM_DESI),
        VideoClip(id = "c2", title = "Sizzling Tawa", colorStartHex = "#FF8008", colorEndHex = "#FFC837", durationMs = 3000L, trimEndMs = 3000L, transitionToNext = TransitionType.ZOOM_IN),
        VideoClip(id = "c3", title = "Tasting Reaction", colorStartHex = "#43C6AC", colorEndHex = "#191654", durationMs = 2500L, trimEndMs = 2500L, filter = VideoFilter.WARM_DESI),
        VideoClip(id = "c4", title = "Location Pin", colorStartHex = "#0052D4", colorEndHex = "#4364F7", durationMs = 2500L, trimEndMs = 2500L)
      ),
      previewTexts = listOf(
        TextOverlay(id = "t1", text = "BEST MUMBAI CHAI ☕", startAtMs = 500L, durationMs = 3500L, positionY = 0.25f, colorHex = "#FFD54F")
      ),
      previewBgm = royaltyFreeAudio[0],
      gradientColors = Pair("#FF8008", "#FFC837")
    ),
    Template(
      id = "tpl_4",
      title = "Attitude Status",
      category = "Attitude",
      tagline = "Dark aesthetic, neon flash & bold font",
      aspectRatio = AspectRatio.RATIO_9_16,
      durationMs = 8000L,
      requiredClipsCount = 3,
      bgmName = "Mumbai Night Drive",
      bpm = 135,
      usesCount = "560K",
      previewClips = listOf(
        VideoClip(id = "c1", title = "Entry", colorStartHex = "#0F2027", colorEndHex = "#203A43", durationMs = 2500L, trimEndMs = 2500L, filter = VideoFilter.MOODY_NOIR, transitionToNext = TransitionType.FLASH),
        VideoClip(id = "c2", title = "Glitch Look", colorStartHex = "#2C3E50", colorEndHex = "#000000", durationMs = 3000L, trimEndMs = 3000L, effect = VideoEffect.GLITCH, transitionToNext = TransitionType.GLITCH),
        VideoClip(id = "c3", title = "Walk Away", colorStartHex = "#1A1A1A", colorEndHex = "#333333", durationMs = 2500L, trimEndMs = 2500L, filter = VideoFilter.CYBER_NEON)
      ),
      previewTexts = listOf(
        TextOverlay(id = "t1", text = "RULES ARE MINE ⚡", startAtMs = 1500L, durationMs = 3000L, positionY = 0.5f, colorHex = "#00E676")
      ),
      previewBgm = royaltyFreeAudio[3],
      gradientColors = Pair("#0F2027", "#2C5364")
    )
  )

  fun getInitialProjects(): List<Project> {
    return listOf(
      Project(
        id = "proj_1",
        name = "Mumbai Sunset Reel",
        aspectRatio = AspectRatio.RATIO_9_16,
        updatedAt = System.currentTimeMillis() - 1000 * 60 * 25, // 25 mins ago
        clips = listOf(
          VideoClip(
            id = "clip_101",
            title = "Marine Drive Skyline",
            colorStartHex = "#FF512F",
            colorEndHex = "#F09819",
            durationMs = 4500L,
            trimEndMs = 4500L,
            filter = VideoFilter.WARM_DESI,
            transitionToNext = TransitionType.DISSOLVE
          ),
          VideoClip(
            id = "clip_102",
            title = "Arabian Sea Waves",
            colorStartHex = "#1A2980",
            colorEndHex = "#26D0CE",
            durationMs = 3500L,
            trimEndMs = 3500L,
            filter = VideoFilter.COOL_TEAL,
            effect = VideoEffect.FILM_GRAIN,
            transitionToNext = TransitionType.ZOOM_IN
          ),
          VideoClip(
            id = "clip_103",
            title = "Cutting Chai Moment",
            colorStartHex = "#D31027",
            colorEndHex = "#EA384D",
            durationMs = 4000L,
            trimEndMs = 4000L,
            filter = VideoFilter.WARM_DESI,
            effect = VideoEffect.VIGNETTE
          )
        ),
        audioTracks = listOf(
          royaltyFreeAudio[0].copy(durationMs = 12000L, volume = 0.85f)
        ),
        textOverlays = listOf(
          TextOverlay(
            id = "txt_101",
            text = "BOMBAY DREAMS 🌆",
            startAtMs = 500L,
            durationMs = 3500L,
            positionY = 0.28f,
            fontSizeSp = 28f,
            colorHex = "#FFD54F"
          ),
          TextOverlay(
            id = "txt_102",
            text = "Best tea in town @ Marine Lines",
            startAtMs = 7500L,
            durationMs = 3500L,
            positionY = 0.72f,
            fontSizeSp = 18f,
            colorHex = "#FFFFFF"
          )
        ),
        stickers = listOf(
          StickerOverlay(
            id = "stk_101",
            emojiOrSymbol = "☕",
            label = "Chai",
            startAtMs = 8000L,
            durationMs = 3000L,
            positionX = 0.8f,
            positionY = 0.65f
          )
        )
      ),
      Project(
        id = "proj_2",
        name = "Bhangra Bass Drop",
        aspectRatio = AspectRatio.RATIO_9_16,
        updatedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 4, // 4 hours ago
        clips = listOf(
          VideoClip(
            id = "clip_201",
            title = "Dhol Intro",
            colorStartHex = "#8E2DE2",
            colorEndHex = "#4A00E0",
            durationMs = 3000L,
            trimEndMs = 3000L,
            transitionToNext = TransitionType.FLASH
          ),
          VideoClip(
            id = "clip_202",
            title = "Beat Drop Step",
            colorStartHex = "#00C9FF",
            colorEndHex = "#92FE9D",
            durationMs = 3500L,
            trimEndMs = 3500L,
            effect = VideoEffect.GLITCH,
            speed = 1.2f,
            transitionToNext = TransitionType.GLITCH
          ),
          VideoClip(
            id = "clip_203",
            title = "Power Freeze",
            colorStartHex = "#F857A6",
            colorEndHex = "#FF5858",
            durationMs = 3000L,
            trimEndMs = 3000L,
            effect = VideoEffect.RGB_SPLIT
          )
        ),
        audioTracks = listOf(
          royaltyFreeAudio[1].copy(durationMs = 9500L)
        ),
        textOverlays = listOf(
          TextOverlay(
            id = "txt_201",
            text = "DHOL BEAT DROP 🔥",
            startAtMs = 2800L,
            durationMs = 3500L,
            positionY = 0.45f,
            fontSizeSp = 32f,
            colorHex = "#00E676"
          )
        )
      ),
      Project(
        id = "proj_3",
        name = "Goa Beach Memories",
        aspectRatio = AspectRatio.RATIO_16_9,
        updatedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 28, // 1 day ago
        clips = listOf(
          VideoClip(
            id = "clip_301",
            title = "Palolem Sunset",
            colorStartHex = "#2193B0",
            colorEndHex = "#6DD5ED",
            durationMs = 5000L,
            trimEndMs = 5000L,
            filter = VideoFilter.BOLLYWOOD,
            transitionToNext = TransitionType.DISSOLVE
          ),
          VideoClip(
            id = "clip_302",
            title = "Scooter Ride",
            colorStartHex = "#CC95C0",
            colorEndHex = "#DBD4B4",
            durationMs = 4500L,
            trimEndMs = 4500L,
            filter = VideoFilter.WARM_DESI
          )
        ),
        audioTracks = listOf(
          royaltyFreeAudio[2].copy(durationMs = 9500L)
        ),
        textOverlays = listOf(
          TextOverlay(
            id = "txt_301",
            text = "Goa Diaries 🌴",
            startAtMs = 500L,
            durationMs = 4000L,
            positionY = 0.5f,
            fontSizeSp = 26f,
            colorHex = "#FFFFFF"
          )
        )
      )
    )
  }
}
