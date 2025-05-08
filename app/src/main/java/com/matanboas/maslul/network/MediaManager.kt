package com.matanboas.maslul.network

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class MediaManager(
    private val trailUid: String,
    private val trailName: String,
    private val context: Context,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val TAG = "MediaManager"

    private sealed class MediaState {
        object Unloaded : MediaState()
        data class Loaded(val imageFolder: String, val thumbnailFolder: String) : MediaState()
    }

    private var state: MediaState = MediaState.Unloaded

    private val cacheDir: File
        get() = context.cacheDir

    private fun getCacheSubDir(type: String): File {
        return File(cacheDir, "trail_images/$trailUid/$type").apply { mkdirs() }
    }

    suspend fun getImagesFolder(): String = withContext(Dispatchers.IO) {
        when (val currentState = state) {
            is MediaState.Loaded -> currentState.imageFolder
            is MediaState.Unloaded -> {
                val cachedImagesDir = getCacheSubDir("images")
                if (cachedImagesDir.listFiles()?.isNotEmpty() == true) {
                    state = MediaState.Loaded(cachedImagesDir.absolutePath, getCacheSubDir("thumbnail").absolutePath)
                    cachedImagesDir.absolutePath
                } else {
                    val imageFolder = fetchImagesFromStorage("images")
                    val thumbnailFolder = fetchImagesFromStorage("thumbnail")
                    state = MediaState.Loaded(imageFolder, thumbnailFolder)
                    imageFolder
                }
            }
        }
    }

    suspend fun getThumbnailsFolder(): String = withContext(Dispatchers.IO) {
        when (val currentState = state) {
            is MediaState.Loaded -> currentState.thumbnailFolder
            is MediaState.Unloaded -> {
                val cachedThumbnailsDir = getCacheSubDir("thumbnail")
                if (cachedThumbnailsDir.listFiles()?.isNotEmpty() == true) {
                    state = MediaState.Loaded(getCacheSubDir("images").absolutePath, cachedThumbnailsDir.absolutePath)
                    cachedThumbnailsDir.absolutePath
                } else {
                    val imageFolder = fetchImagesFromStorage("images")
                    val thumbnailFolder = fetchImagesFromStorage("thumbnail")
                    state = MediaState.Loaded(imageFolder, thumbnailFolder)
                    thumbnailFolder
                }
            }
        }
    }

    private fun getCachedImages(type: String): List<String> {
        val dir = getCacheSubDir(type)
        return dir.listFiles()?.filter { it.isFile && it.lastModified() > System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 }
            ?.map { it.absolutePath }
            ?: emptyList()
    }

    private suspend fun fetchImagesFromStorage(type: String): String = withContext(Dispatchers.IO) {
        val sanitizedTrailName = trailName.replace(" ", "_").replace("/", "_")
        val folderPath = "$type/$sanitizedTrailName"
        val folderRef = storage.reference.child(folderPath)
        val cacheDir = getCacheSubDir(type)

        try {
            val result = folderRef.listAll().await()
            if (result.items.isEmpty()) {
                Log.d(TAG, "No $type found in $folderPath for trail '$trailName'")
                return@withContext cacheDir.absolutePath
            }

            result.items.forEach { itemRef ->
                try {
                    val url = itemRef.downloadUrl.await().toString()
                    cacheImage(url, type)
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching $type URL for ${itemRef.path}", e)
                }
            }
            Log.d(TAG, "Fetched ${result.items.size} $type for trail '$trailName' from $folderPath")
            cacheDir.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error listing $type in $folderPath for trail '$trailName'", e)
            cacheDir.absolutePath
        }
    }

    private suspend fun cacheImage(url: String, type: String): File? = withContext(Dispatchers.IO) {
        try {
            val file = File(getCacheSubDir(type), UUID.randomUUID().toString())
            val bytes = java.net.URL(url).readBytes()
            file.writeBytes(bytes)
            Log.d("MediaManager", "Cached $type image: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e("MediaManager", "Error caching $type from $url", e)
            null
        }
    }

    companion object {
        fun cleanupOldImages(context: Context) {
            val cacheDir = File(context.cacheDir, "trail_images")
            if (!cacheDir.exists()) return

            cacheDir.listFiles()?.forEach { trailDir ->
                trailDir.listFiles()?.forEach { typeDir ->
                    typeDir.listFiles()?.forEach { file ->
                        if (file.lastModified() < System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) {
                            file.delete()
                            Log.d("MediaManager", "Deleted old cached image: ${file.absolutePath}")
                        }
                    }
                    if (typeDir.listFiles()?.isEmpty() == true) typeDir.delete()
                }
                if (trailDir.listFiles()?.isEmpty() == true) trailDir.delete()
            }
        }
    }
}