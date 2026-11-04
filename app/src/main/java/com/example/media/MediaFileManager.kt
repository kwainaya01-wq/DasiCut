package com.example.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.model.AudioTrack
import com.example.model.VideoClip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object MediaFileManager {

  private val retrieverCache = mutableMapOf<String, MediaMetadataRetriever>()

  suspend fun importVideoFromUri(context: Context, uri: Uri): VideoClip? = withContext(Dispatchers.IO) {
    try {
      val videoDir = File(context.filesDir, "videos").apply { mkdirs() }
      val thumbDir = File(context.filesDir, "thumbs").apply { mkdirs() }

      val fileId = UUID.randomUUID().toString().take(8)
      val destFile = File(videoDir, "video_$fileId.mp4")

      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destFile).use { output ->
          input.copyTo(output)
        }
      } ?: return@withContext null

      val retriever = MediaMetadataRetriever()
      retriever.setDataSource(destFile.absolutePath)

      val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
      val durationMs = durationStr?.toLongOrNull()?.coerceAtLeast(1000L) ?: 4000L

      val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
      val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
      val width = widthStr?.toIntOrNull() ?: 1080
      val height = heightStr?.toIntOrNull() ?: 1920

      // Extract real thumbnail frame at 500ms or 0
      val thumbFile = File(thumbDir, "thumb_$fileId.jpg")
      val frameTimeUs = (minOf(500L, durationMs / 2) * 1000)
      val frameBitmap = retriever.getFrameAtTime(frameTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        ?: retriever.frameAtTime

      frameBitmap?.let { bmp ->
        FileOutputStream(thumbFile).use { fos ->
          bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos)
        }
      }

      retriever.release()

      val name = try {
        uri.lastPathSegment?.substringAfterLast('/')?.take(20) ?: "Imported Video"
      } catch (_: Exception) {
        "Imported Video"
      }

      VideoClip(
        id = "clip_$fileId",
        title = name,
        colorStartHex = "#00B09B",
        colorEndHex = "#004D40",
        localUri = destFile.absolutePath,
        durationMs = durationMs,
        trimStartMs = 0L,
        trimEndMs = durationMs
      )
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  suspend fun importAudioFromUri(context: Context, uri: Uri): AudioTrack? = withContext(Dispatchers.IO) {
    try {
      val audioDir = File(context.filesDir, "audio").apply { mkdirs() }
      val fileId = UUID.randomUUID().toString().take(8)
      val destFile = File(audioDir, "audio_$fileId.mp3")

      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destFile).use { output ->
          input.copyTo(output)
        }
      } ?: return@withContext null

      val retriever = MediaMetadataRetriever()
      retriever.setDataSource(destFile.absolutePath)

      val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
      val durationMs = durationStr?.toLongOrNull()?.coerceAtLeast(1000L) ?: 10000L

      val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        ?: uri.lastPathSegment?.substringAfterLast('/')?.take(20) ?: "Imported Audio"
      val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Device Audio"

      retriever.release()

      AudioTrack(
        id = "audio_$fileId",
        title = title,
        artist = artist,
        category = "Imported",
        startAtMs = 0L,
        durationMs = durationMs,
        waveformPoints = listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 0.6f, 0.9f, 0.7f, 0.5f, 0.8f)
      )
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun getFrameAtTime(path: String, timeMs: Long): Bitmap? {
    return try {
      val retriever = retrieverCache.getOrPut(path) {
        val r = MediaMetadataRetriever()
        r.setDataSource(path)
        r
      }
      val timeUs = timeMs * 1000L
      retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (e: Exception) {
      null
    }
  }

  fun clearCache() {
    retrieverCache.values.forEach {
      try { it.release() } catch (_: Exception) {}
    }
    retrieverCache.clear()
  }
}
