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

  init {
    loadProjects()
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
      tObj.put("fontStyleName", t.fontStyleName)
      tObj.put("animationType", t.animationType.name)
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
            fontStyleName = tObj.optString("fontStyleName", "Outfit Bold"),
            animationType = try {
              com.example.model.TextAnimation.valueOf(tObj.optString("animationType", "POP"))
            } catch (_: Exception) { com.example.model.TextAnimation.POP }
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
            positionY = sObj.optDouble("positionY", 0.75).toFloat()
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
}
