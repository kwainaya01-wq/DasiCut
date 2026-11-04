package com.example.data

import android.content.Context
import com.example.model.AspectRatio
import com.example.model.Project
import com.example.model.VideoClip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class ProjectRepository(private val context: Context) {

  private val _projects = MutableStateFlow<List<Project>>(emptyList())
  val projects: StateFlow<List<Project>> = _projects.asStateFlow()

  private val projectsFile = File(context.filesDir, "desicut_projects.json")

  private val presetsFile = File(context.filesDir, "desicut_effects_presets.json")
  private val _effectPresets = MutableStateFlow<List<com.example.model.EffectPreset>>(emptyList())
  val effectPresets: StateFlow<List<com.example.model.EffectPreset>> = _effectPresets.asStateFlow()

  private val templatesFile = File(context.filesDir, "desicut_custom_templates.json")
  private val _customTemplates = MutableStateFlow<List<com.example.model.CustomTemplate>>(emptyList())
  val customTemplates: StateFlow<List<com.example.model.CustomTemplate>> = _customTemplates.asStateFlow()

  private val _isOwnerMode = MutableStateFlow(true)
  val isOwnerMode: StateFlow<Boolean> = _isOwnerMode.asStateFlow()

  fun setOwnerMode(enabled: Boolean) {
    _isOwnerMode.value = enabled
  }

  init {
    loadProjects()
    loadEffectPresets()
    loadCustomTemplates()
  }

  private fun loadProjects() {
    try {
      if (projectsFile.exists()) {
        val jsonStr = projectsFile.readText()
        if (jsonStr.isNotBlank()) {
          val jsonArray = JSONArray(jsonStr)
          val list = mutableListOf<Project>()
          for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(deserializeProject(obj))
          }
          if (list.isNotEmpty()) {
            _projects.value = list
            return
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    // Fallback to sample data
    val initial = SampleData.getInitialProjects()
    _projects.value = initial
    saveToDisk(initial)
  }

  fun saveProject(project: Project) {
    val current = _projects.value.toMutableList()
    val index = current.indexOfFirst { it.id == project.id }
    val updated = project.copy(updatedAt = System.currentTimeMillis())
    if (index >= 0) {
      current[index] = updated
    } else {
      current.add(0, updated)
    }
    _projects.value = current
    saveToDisk(current)
  }

  fun deleteProject(projectId: String) {
    val filtered = _projects.value.filter { it.id != projectId }
    _projects.value = filtered
    saveToDisk(filtered)
  }

  fun duplicateProject(projectId: String): Project? {
    val orig = _projects.value.find { it.id == projectId } ?: return null
    val copy = orig.copy(
      id = "proj_${UUID.randomUUID().toString().take(8)}",
      name = "${orig.name} (Copy)",
      updatedAt = System.currentTimeMillis()
    )
    val list = _projects.value.toMutableList()
    list.add(0, copy)
    _projects.value = list
    saveToDisk(list)
    return copy
  }

  fun createNewProject(name: String = "Untitled Video", aspectRatio: AspectRatio = AspectRatio.RATIO_9_16): Project {
    val newProj = Project(
      id = "proj_${UUID.randomUUID().toString().take(8)}",
      name = name,
      aspectRatio = aspectRatio,
      updatedAt = System.currentTimeMillis(),
      clips = listOf(
        VideoClip(
          id = "clip_${UUID.randomUUID().toString().take(6)}",
          title = "Clip 1",
          colorStartHex = "#00B09B",
          colorEndHex = "#96C93D",
          durationMs = 4000L,
          trimEndMs = 4000L
        )
      )
    )
    val list = _projects.value.toMutableList()
    list.add(0, newProj)
    _projects.value = list
    saveToDisk(list)
    return newProj
  }

  private fun saveToDisk(list: List<Project>) {
    try {
      val jsonArray = JSONArray()
      for (p in list) {
        jsonArray.put(serializeProject(p))
      }
      projectsFile.writeText(jsonArray.toString(2))
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun serializeProject(p: Project): JSONObject {
    val obj = JSONObject()
    obj.put("id", p.id)
    obj.put("name", p.name)
    obj.put("aspectRatio", p.aspectRatio.name)
    obj.put("updatedAt", p.updatedAt)
    obj.put("quality", p.quality)
    obj.put("fps", p.fps)

    val clipsArr = JSONArray()
    for (c in p.clips) {
      val cObj = JSONObject()
      cObj.put("id", c.id)
      cObj.put("title", c.title)
      cObj.put("colorStartHex", c.colorStartHex)
      cObj.put("colorEndHex", c.colorEndHex)
      cObj.put("localUri", c.localUri ?: "")
      cObj.put("durationMs", c.durationMs)
      cObj.put("trimStartMs", c.trimStartMs)
      cObj.put("trimEndMs", c.trimEndMs)
      cObj.put("speed", c.speed.toDouble())
      cObj.put("volume", c.volume.toDouble())
      cObj.put("isMuted", c.isMuted)
      cObj.put("rotationDegrees", c.rotationDegrees)
      cObj.put("isFlippedH", c.isFlippedH)
      cObj.put("isFlippedV", c.isFlippedV)
      cObj.put("isReversed", c.isReversed)
      cObj.put("transitionToNext", c.transitionToNext.name)
      cObj.put("transitionDurationMs", c.transitionDurationMs)
      cObj.put("filter", c.filter.name)
      cObj.put("filterIntensity", c.filterIntensity.toDouble())
      cObj.put("effect", c.effect.name)
      cObj.put("effectIntensity", c.effectIntensity.toDouble())

      val fxArr = JSONArray()
      for (fx in c.effects) {
        val fxObj = JSONObject()
        fxObj.put("id", fx.id)
        fxObj.put("effect", fx.effect.name)
        fxObj.put("intensity", fx.intensity.toDouble())
        fxArr.put(fxObj)
      }
      cObj.put("effects", fxArr)

      val retObj = JSONObject()
      retObj.put("isEnabled", c.retouch.isEnabled)
      retObj.put("overallIntensity", c.retouch.overallIntensity.toDouble())
      retObj.put("skinSmoothing", c.retouch.skinSmoothing.toDouble())
      retObj.put("faceBrightness", c.retouch.faceBrightness.toDouble())
      retObj.put("blemishReduction", c.retouch.blemishReduction.toDouble())
      retObj.put("eyeEnhancement", c.retouch.eyeEnhancement.toDouble())
      retObj.put("naturalSkinTone", c.retouch.naturalSkinTone.toDouble())
      cObj.put("retouch", retObj)

      cObj.put("fadeInMs", c.fadeInMs)
      cObj.put("fadeOutMs", c.fadeOutMs)
      cObj.put("isFrozen", c.isFrozen)
      clipsArr.put(cObj)
    }
    obj.put("clips", clipsArr)

    val audioArr = JSONArray()
    for (a in p.audioTracks) {
      val aObj = JSONObject()
      aObj.put("id", a.id)
      aObj.put("title", a.title)
      aObj.put("artist", a.artist)
      aObj.put("category", a.category)
      aObj.put("startAtMs", a.startAtMs)
      aObj.put("durationMs", a.durationMs)
      aObj.put("volume", a.volume.toDouble())
      aObj.put("isMuted", a.isMuted)
      audioArr.put(aObj)
    }
    obj.put("audioTracks", audioArr)

    val textArr = JSONArray()
    for (t in p.textOverlays) {
      val tObj = JSONObject()
      tObj.put("id", t.id)
      tObj.put("text", t.text)
      tObj.put("startAtMs", t.startAtMs)
      tObj.put("durationMs", t.durationMs)
      tObj.put("positionX", t.positionX.toDouble())
      tObj.put("positionY", t.positionY.toDouble())
      tObj.put("fontSizeSp", t.fontSizeSp.toDouble())
      tObj.put("colorHex", t.colorHex)
      tObj.put("bgColorHex", t.bgColorHex ?: "")
      tObj.put("hasStroke", t.hasStroke)
      tObj.put("strokeColorHex", t.strokeColorHex)
      tObj.put("fontStyleName", t.fontStyleName)
      tObj.put("animationType", t.animationType.name)
      tObj.put("rotationDegrees", t.rotationDegrees.toDouble())
      tObj.put("scale", t.scale.toDouble())
      tObj.put("alignment", t.alignment)
      tObj.put("hasShadow", t.hasShadow)
      tObj.put("shadowColorHex", t.shadowColorHex)
      textArr.put(tObj)
    }
    obj.put("textOverlays", textArr)

    val stkArr = JSONArray()
    for (s in p.stickers) {
      val sObj = JSONObject()
      sObj.put("id", s.id)
      sObj.put("emojiOrSymbol", s.emojiOrSymbol)
      sObj.put("label", s.label)
      sObj.put("startAtMs", s.startAtMs)
      sObj.put("durationMs", s.durationMs)
      sObj.put("positionX", s.positionX.toDouble())
      sObj.put("positionY", s.positionY.toDouble())
      sObj.put("scale", s.scale.toDouble())
      sObj.put("rotationDegrees", s.rotationDegrees.toDouble())
      sObj.put("hasShadow", s.hasShadow)
      stkArr.put(sObj)
    }
    obj.put("stickers", stkArr)

    return obj
  }

  private fun deserializeProject(obj: JSONObject): Project {
    val id = obj.optString("id", UUID.randomUUID().toString())
    val name = obj.optString("name", "Untitled")
    val ratioStr = obj.optString("aspectRatio", AspectRatio.RATIO_9_16.name)
    val ratio = try { AspectRatio.valueOf(ratioStr) } catch (_: Exception) { AspectRatio.RATIO_9_16 }
    val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
    val quality = obj.optString("quality", "1080p")
    val fps = obj.optInt("fps", 30)

    val clips = mutableListOf<VideoClip>()
    val clipsArr = obj.optJSONArray("clips")
    if (clipsArr != null) {
      for (i in 0 until clipsArr.length()) {
        val cObj = clipsArr.getJSONObject(i)
        clips.add(
          VideoClip(
            id = cObj.optString("id", "c_$i"),
            title = cObj.optString("title", "Clip ${i + 1}"),
            colorStartHex = cObj.optString("colorStartHex", "#FF512F"),
            colorEndHex = cObj.optString("colorEndHex", "#DD2476"),
            localUri = cObj.optString("localUri").takeIf { it.isNotBlank() },
            durationMs = cObj.optLong("durationMs", 4000L),
            trimStartMs = cObj.optLong("trimStartMs", 0L),
            trimEndMs = cObj.optLong("trimEndMs", cObj.optLong("durationMs", 4000L)),
            speed = cObj.optDouble("speed", 1.0).toFloat(),
            volume = cObj.optDouble("volume", 1.0).toFloat(),
            isMuted = cObj.optBoolean("isMuted", false),
            rotationDegrees = cObj.optInt("rotationDegrees", 0),
            isFlippedH = cObj.optBoolean("isFlippedH", false),
            isFlippedV = cObj.optBoolean("isFlippedV", false),
            isReversed = cObj.optBoolean("isReversed", false),
            transitionToNext = try {
              com.example.model.TransitionType.valueOf(cObj.optString("transitionToNext", "NONE"))
            } catch (_: Exception) { com.example.model.TransitionType.NONE },
            transitionDurationMs = cObj.optLong("transitionDurationMs", 500L),
            filter = try {
              com.example.model.VideoFilter.valueOf(cObj.optString("filter", "ORIGINAL"))
            } catch (_: Exception) { com.example.model.VideoFilter.ORIGINAL },
            filterIntensity = cObj.optDouble("filterIntensity", 1.0).toFloat(),
            effect = try {
              com.example.model.VideoEffect.valueOf(cObj.optString("effect", "NONE"))
            } catch (_: Exception) { com.example.model.VideoEffect.NONE },
            effectIntensity = cObj.optDouble("effectIntensity", 0.8).toFloat(),
            effects = run {
              val fxList = mutableListOf<com.example.model.AppliedEffect>()
              val fxArr = cObj.optJSONArray("effects")
              if (fxArr != null) {
                for (fxIdx in 0 until fxArr.length()) {
                  val fxItem = fxArr.optJSONObject(fxIdx) ?: continue
                  val effectType = try {
                    com.example.model.VideoEffect.valueOf(fxItem.optString("effect", "NONE"))
                  } catch (_: Exception) { com.example.model.VideoEffect.NONE }
                  if (effectType != com.example.model.VideoEffect.NONE) {
                    fxList.add(
                      com.example.model.AppliedEffect(
                        id = fxItem.optString("id", "fx_$fxIdx"),
                        effect = effectType,
                        intensity = fxItem.optDouble("intensity", 0.8).toFloat()
                      )
                    )
                  }
                }
              }
              fxList
            },
            retouch = run {
              val rObj = cObj.optJSONObject("retouch")
              if (rObj != null) {
                com.example.model.FaceRetouchSettings(
                  isEnabled = rObj.optBoolean("isEnabled", false),
                  overallIntensity = rObj.optDouble("overallIntensity", 0.70).toFloat(),
                  skinSmoothing = rObj.optDouble("skinSmoothing", 0.65).toFloat(),
                  faceBrightness = rObj.optDouble("faceBrightness", 0.50).toFloat(),
                  blemishReduction = rObj.optDouble("blemishReduction", 0.55).toFloat(),
                  eyeEnhancement = rObj.optDouble("eyeEnhancement", 0.50).toFloat(),
                  naturalSkinTone = rObj.optDouble("naturalSkinTone", 0.60).toFloat()
                )
              } else {
                com.example.model.FaceRetouchSettings()
              }
            },
            fadeInMs = cObj.optLong("fadeInMs", 0L),
            fadeOutMs = cObj.optLong("fadeOutMs", 0L),
            isFrozen = cObj.optBoolean("isFrozen", false)
          )
        )
      }
    }

    val audioTracks = mutableListOf<com.example.model.AudioTrack>()
    val audioArr = obj.optJSONArray("audioTracks")
    if (audioArr != null) {
      for (i in 0 until audioArr.length()) {
        val aObj = audioArr.getJSONObject(i)
        audioTracks.add(
          com.example.model.AudioTrack(
            id = aObj.optString("id", "a_$i"),
            title = aObj.optString("title", "Audio Track"),
            artist = aObj.optString("artist", "DesiCut"),
            category = aObj.optString("category", "BGM"),
            startAtMs = aObj.optLong("startAtMs", 0L),
            durationMs = aObj.optLong("durationMs", 10000L),
            volume = aObj.optDouble("volume", 1.0).toFloat(),
            isMuted = aObj.optBoolean("isMuted", false)
          )
        )
      }
    }

    val textOverlays = mutableListOf<com.example.model.TextOverlay>()
    val textArr = obj.optJSONArray("textOverlays")
    if (textArr != null) {
      for (i in 0 until textArr.length()) {
        val tObj = textArr.getJSONObject(i)
        textOverlays.add(
          com.example.model.TextOverlay(
            id = tObj.optString("id", "t_$i"),
            text = tObj.optString("text", "Text"),
            startAtMs = tObj.optLong("startAtMs", 0L),
            durationMs = tObj.optLong("durationMs", 3000L),
            positionX = tObj.optDouble("positionX", 0.5).toFloat(),
            positionY = tObj.optDouble("positionY", 0.5).toFloat(),
            fontSizeSp = tObj.optDouble("fontSizeSp", 24.0).toFloat(),
            colorHex = tObj.optString("colorHex", "#FFFFFF"),
            bgColorHex = tObj.optString("bgColorHex").takeIf { it.isNotBlank() },
            hasStroke = tObj.optBoolean("hasStroke", true),
            strokeColorHex = tObj.optString("strokeColorHex", "#000000"),
            fontStyleName = tObj.optString("fontStyleName", "Outfit Bold"),
            animationType = try {
              com.example.model.TextAnimation.valueOf(tObj.optString("animationType", "POP"))
            } catch (_: Exception) { com.example.model.TextAnimation.POP },
            rotationDegrees = tObj.optDouble("rotationDegrees", 0.0).toFloat(),
            scale = tObj.optDouble("scale", 1.0).toFloat(),
            alignment = tObj.optString("alignment", "CENTER"),
            hasShadow = tObj.optBoolean("hasShadow", false),
            shadowColorHex = tObj.optString("shadowColorHex", "#000000")
          )
        )
      }
    }

    val stickers = mutableListOf<com.example.model.StickerOverlay>()
    val stkArr = obj.optJSONArray("stickers")
    if (stkArr != null) {
      for (i in 0 until stkArr.length()) {
        val sObj = stkArr.getJSONObject(i)
        stickers.add(
          com.example.model.StickerOverlay(
            id = sObj.optString("id", "s_$i"),
            emojiOrSymbol = sObj.optString("emojiOrSymbol", "✨"),
            label = sObj.optString("label", "Sticker"),
            startAtMs = sObj.optLong("startAtMs", 0L),
            durationMs = sObj.optLong("durationMs", 3000L),
            positionX = sObj.optDouble("positionX", 0.5).toFloat(),
            positionY = sObj.optDouble("positionY", 0.75).toFloat(),
            scale = sObj.optDouble("scale", 1.2).toFloat(),
            rotationDegrees = sObj.optDouble("rotationDegrees", 0.0).toFloat(),
            hasShadow = sObj.optBoolean("hasShadow", false)
          )
        )
      }
    }

    return Project(
      id = id,
      name = name,
      aspectRatio = ratio,
      updatedAt = updatedAt,
      clips = clips,
      audioTracks = audioTracks,
      textOverlays = textOverlays,
      stickers = stickers,
      quality = quality,
      fps = fps
    )
  }

  // ==========================================
  // OWNER-CONTROLLED EFFECT PRESETS REPOSITORY
  // ==========================================

  fun loadEffectPresets() {
    try {
      if (presetsFile.exists()) {
        val jsonStr = presetsFile.readText()
        if (jsonStr.isNotBlank()) {
          val arr = JSONArray(jsonStr)
          val list = mutableListOf<com.example.model.EffectPreset>()
          for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val fxList = mutableListOf<com.example.model.AppliedEffect>()
            val fxArr = obj.optJSONArray("effects")
            if (fxArr != null) {
              for (j in 0 until fxArr.length()) {
                val fObj = fxArr.getJSONObject(j)
                val effectType = try {
                  com.example.model.VideoEffect.valueOf(fObj.optString("effect", "NONE"))
                } catch (_: Exception) { com.example.model.VideoEffect.NONE }
                if (effectType != com.example.model.VideoEffect.NONE) {
                  fxList.add(
                    com.example.model.AppliedEffect(
                      id = fObj.optString("id", "fx_$j"),
                      effect = effectType,
                      intensity = fObj.optDouble("intensity", 0.8).toFloat()
                    )
                  )
                }
              }
            }
            list.add(
              com.example.model.EffectPreset(
                id = obj.optString("id", UUID.randomUUID().toString()),
                name = obj.optString("name", "Custom Preset"),
                description = obj.optString("description", ""),
                category = obj.optString("category", "Cinematic"),
                thumbnailIcon = obj.optString("thumbnailIcon", "✨"),
                effects = fxList,
                isPublished = obj.optBoolean("isPublished", true),
                isDraft = obj.optBoolean("isDraft", false),
                createdBy = obj.optString("createdBy", "Studio Owner"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
              )
            )
          }
          if (list.isNotEmpty()) {
            _effectPresets.value = list
            return
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    // Default built-in presets created by studio owner
    val defaults = getDefaultEffectPresets()
    _effectPresets.value = defaults
    savePresetsToDisk(defaults)
  }

  fun saveEffectPreset(preset: com.example.model.EffectPreset) {
    val current = _effectPresets.value.toMutableList()
    val idx = current.indexOfFirst { it.id == preset.id }
    if (idx >= 0) {
      current[idx] = preset
    } else {
      current.add(0, preset)
    }
    _effectPresets.value = current
    savePresetsToDisk(current)
  }

  fun deleteEffectPreset(presetId: String) {
    val current = _effectPresets.value.filter { it.id != presetId }
    _effectPresets.value = current
    savePresetsToDisk(current)
  }

  fun publishEffectPreset(presetId: String, publish: Boolean) {
    val current = _effectPresets.value.map {
      if (it.id == presetId) it.copy(isPublished = publish, isDraft = !publish) else it
    }
    _effectPresets.value = current
    savePresetsToDisk(current)
  }

  private fun savePresetsToDisk(list: List<com.example.model.EffectPreset>) {
    try {
      val arr = JSONArray()
      for (p in list) {
        val obj = JSONObject()
        obj.put("id", p.id)
        obj.put("name", p.name)
        obj.put("description", p.description)
        obj.put("category", p.category)
        obj.put("thumbnailIcon", p.thumbnailIcon)
        obj.put("isPublished", p.isPublished)
        obj.put("isDraft", p.isDraft)
        obj.put("createdBy", p.createdBy)
        obj.put("createdAt", p.createdAt)
        val fxArr = JSONArray()
        for (fx in p.effects) {
          val fObj = JSONObject()
          fObj.put("id", fx.id)
          fObj.put("effect", fx.effect.name)
          fObj.put("intensity", fx.intensity.toDouble())
          fxArr.put(fObj)
        }
        obj.put("effects", fxArr)
        arr.put(obj)
      }
      presetsFile.writeText(arr.toString(2))
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun getDefaultEffectPresets(): List<com.example.model.EffectPreset> {
    return listOf(
      com.example.model.EffectPreset(
        id = "preset_glam",
        name = "Bollywood Glam Aura",
        description = "Radiant golden halo, dreamy highlight bloom and soft portrait focus",
        category = "Cinematic",
        thumbnailIcon = "👑",
        effects = listOf(
          com.example.model.AppliedEffect(id = "fx_1", effect = com.example.model.VideoEffect.CINEMATIC_GLOW, intensity = 0.85f),
          com.example.model.AppliedEffect(id = "fx_2", effect = com.example.model.VideoEffect.GOLDEN_LIGHT, intensity = 0.70f),
          com.example.model.AppliedEffect(id = "fx_3", effect = com.example.model.VideoEffect.SOFT_FOCUS, intensity = 0.60f)
        ),
        isPublished = true
      ),
      com.example.model.EffectPreset(
        id = "preset_neon",
        name = "Cyber Neon Pulse",
        description = "Futuristic RGB displacement, rolling scanwaves and electric aura",
        category = "Glitch",
        thumbnailIcon = "⚡",
        effects = listOf(
          com.example.model.AppliedEffect(id = "fx_4", effect = com.example.model.VideoEffect.NEON_GLOW, intensity = 0.90f),
          com.example.model.AppliedEffect(id = "fx_5", effect = com.example.model.VideoEffect.RGB_SPLIT, intensity = 0.75f),
          com.example.model.AppliedEffect(id = "fx_6", effect = com.example.model.VideoEffect.SCANLINE_WAVE, intensity = 0.50f)
        ),
        isPublished = true
      ),
      com.example.model.EffectPreset(
        id = "preset_desi",
        name = "Desi Sunset Chai",
        description = "Warm amber sun rays, deep film vignette and organic 35mm grain",
        category = "Retro",
        thumbnailIcon = "🌅",
        effects = listOf(
          com.example.model.AppliedEffect(id = "fx_7", effect = com.example.model.VideoEffect.SUN_RAYS, intensity = 0.80f),
          com.example.model.AppliedEffect(id = "fx_8", effect = com.example.model.VideoEffect.MOVIE_GRAIN, intensity = 0.65f),
          com.example.model.AppliedEffect(id = "fx_9", effect = com.example.model.VideoEffect.VIGNETTE, intensity = 0.70f)
        ),
        isPublished = true
      ),
      com.example.model.EffectPreset(
        id = "preset_shake",
        name = "Retro Tape Shake",
        description = "Analog magnetic VHS artifacts with rhythmic beat camera pulse",
        category = "Shake & Camera",
        thumbnailIcon = "📼",
        effects = listOf(
          com.example.model.AppliedEffect(id = "fx_10", effect = com.example.model.VideoEffect.CAMERA_SHAKE, intensity = 0.75f),
          com.example.model.AppliedEffect(id = "fx_11", effect = com.example.model.VideoEffect.VHS_GLITCH, intensity = 0.80f)
        ),
        isPublished = true
      ),
      com.example.model.EffectPreset(
        id = "preset_body",
        name = "Neon Body Clone",
        description = "Cyber stroke silhouettes and luminous astral clone trail",
        category = "Body & Motion",
        thumbnailIcon = "👤",
        effects = listOf(
          com.example.model.AppliedEffect(id = "fx_12", effect = com.example.model.VideoEffect.BODY_NEON_STROKE, intensity = 0.85f),
          com.example.model.AppliedEffect(id = "fx_13", effect = com.example.model.VideoEffect.SHADOW_CLONE, intensity = 0.70f)
        ),
        isPublished = true
      )
    )
  }

  // ==========================================
  // OWNER-CONTROLLED TEMPLATES REPOSITORY
  // ==========================================

  fun loadCustomTemplates() {
    try {
      if (templatesFile.exists()) {
        val jsonStr = templatesFile.readText()
        if (jsonStr.isNotBlank()) {
          val arr = JSONArray(jsonStr)
          val list = mutableListOf<com.example.model.CustomTemplate>()
          for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val ratioStr = obj.optString("aspectRatio", AspectRatio.RATIO_9_16.name)
            val ratio = try { AspectRatio.valueOf(ratioStr) } catch (_: Exception) { AspectRatio.RATIO_9_16 }
            list.add(
              com.example.model.CustomTemplate(
                id = obj.optString("id", "tpl_${UUID.randomUUID().toString().take(6)}"),
                title = obj.optString("title", "Custom Template"),
                category = obj.optString("category", "Trending"),
                tagline = obj.optString("tagline", "DesiCut Studio Official"),
                aspectRatio = ratio,
                durationMs = obj.optLong("durationMs", 8000L),
                requiredClipsCount = obj.optInt("requiredClipsCount", 3),
                bgmName = obj.optString("bgmName", "Desi Beat"),
                bpm = obj.optInt("bpm", 128),
                usesCount = obj.optString("usesCount", "1.2k"),
                gradientStartHex = obj.optString("gradientStartHex", "#0A2E20"),
                gradientEndHex = obj.optString("gradientEndHex", "#051610"),
                isPublished = obj.optBoolean("isPublished", true),
                isDraft = obj.optBoolean("isDraft", false),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
              )
            )
          }
          if (list.isNotEmpty()) {
            _customTemplates.value = list
            return
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    val defaults = getDefaultCustomTemplates()
    _customTemplates.value = defaults
    saveCustomTemplatesToDisk(defaults)
  }

  fun saveCustomTemplate(template: com.example.model.CustomTemplate) {
    val current = _customTemplates.value.toMutableList()
    val idx = current.indexOfFirst { it.id == template.id }
    if (idx >= 0) {
      current[idx] = template
    } else {
      current.add(0, template)
    }
    _customTemplates.value = current
    saveCustomTemplatesToDisk(current)
  }

  fun deleteCustomTemplate(templateId: String) {
    val current = _customTemplates.value.filter { it.id != templateId }
    _customTemplates.value = current
    saveCustomTemplatesToDisk(current)
  }

  fun publishCustomTemplate(templateId: String, publish: Boolean) {
    val current = _customTemplates.value.map {
      if (it.id == templateId) it.copy(isPublished = publish, isDraft = !publish) else it
    }
    _customTemplates.value = current
    saveCustomTemplatesToDisk(current)
  }

  private fun saveCustomTemplatesToDisk(list: List<com.example.model.CustomTemplate>) {
    try {
      val arr = JSONArray()
      for (t in list) {
        val obj = JSONObject()
        obj.put("id", t.id)
        obj.put("title", t.title)
        obj.put("category", t.category)
        obj.put("tagline", t.tagline)
        obj.put("aspectRatio", t.aspectRatio.name)
        obj.put("durationMs", t.durationMs)
        obj.put("requiredClipsCount", t.requiredClipsCount)
        obj.put("bgmName", t.bgmName)
        obj.put("bpm", t.bpm)
        obj.put("usesCount", t.usesCount)
        obj.put("gradientStartHex", t.gradientStartHex)
        obj.put("gradientEndHex", t.gradientEndHex)
        obj.put("isPublished", t.isPublished)
        obj.put("isDraft", t.isDraft)
        obj.put("createdAt", t.createdAt)
        arr.put(obj)
      }
      templatesFile.writeText(arr.toString(2))
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun getDefaultCustomTemplates(): List<com.example.model.CustomTemplate> {
    return listOf(
      com.example.model.CustomTemplate(
        id = "tpl_desi_beat_drop",
        title = "Desi Beat Drop 🥁",
        category = "Beat Sync",
        tagline = "Sync clips with high-energy Dhol beats",
        durationMs = 9000L,
        requiredClipsCount = 4,
        bgmName = "Bhangra Dhol Drop (132 BPM)",
        gradientStartHex = "#FF416C",
        gradientEndHex = "#8A2387",
        isPublished = true
      ),
      com.example.model.CustomTemplate(
        id = "tpl_cinematic_travel",
        title = "Kashmir to Kanyakumari 🏔️",
        category = "Travel",
        tagline = "Epic aerial landscape flow with cinematic grading",
        durationMs = 12000L,
        requiredClipsCount = 5,
        bgmName = "Flute & Santoor Ambient (85 BPM)",
        gradientStartHex = "#00C9FF",
        gradientEndHex = "#92FE9D",
        isPublished = true
      ),
      com.example.model.CustomTemplate(
        id = "tpl_wedding_glam",
        title = "Royal Indian Wedding 💍",
        category = "Status",
        tagline = "Slow-mo luxury jewelry & outfit showcase",
        durationMs = 15000L,
        requiredClipsCount = 6,
        bgmName = "Shehnai & Sitar Harmony (92 BPM)",
        gradientStartHex = "#F37335",
        gradientEndHex = "#FDC830",
        isPublished = true
      )
    )
  }
}
